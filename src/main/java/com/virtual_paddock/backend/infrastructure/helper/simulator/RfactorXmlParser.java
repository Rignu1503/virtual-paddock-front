package com.virtual_paddock.backend.infrastructure.helper.simulator;

import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkItemRequest;
import com.virtual_paddock.backend.domain.entities.Driver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Order(20)
public class RfactorXmlParser implements ISimulatorLogParser {

    @Override
    public boolean supports(String filename, String content) {
        String lowerName = filename != null ? filename.toLowerCase() : "";
        String trimmed = content != null ? content.trim() : "";
        return lowerName.endsWith(".xml") || trimmed.startsWith("<");
    }

    @Override
    public List<RaceResultBulkItemRequest> parse(String content, List<Driver> allDrivers) throws Exception {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();
        String sanitizedXml = content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        dbFactory.setNamespaceAware(false);
        try {
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception ignored) {}

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(sanitizedXml));
        Document doc = dBuilder.parse(inputSource);
        doc.getDocumentElement().normalize();

        NodeList driverNodes = null;
        NodeList raceNodes = doc.getElementsByTagName("Race");
        if (raceNodes.getLength() > 0) {
            Element raceEl = (Element) raceNodes.item(0);
            driverNodes = raceEl.getElementsByTagName("Driver");
        }
        if (driverNodes == null || driverNodes.getLength() == 0) {
            driverNodes = doc.getElementsByTagName("Driver");
        }

        for (int i = 0; i < driverNodes.getLength(); i++) {
            Element driverEl = (Element) driverNodes.item(i);

            String name = getTagValue(driverEl, "Name");
            String carNumber = getTagValue(driverEl, "CarNumber");
            String positionStr = getTagValue(driverEl, "Position");
            if (positionStr == null) {
                positionStr = getTagValue(driverEl, "ClassPosition");
            }
            Integer finishPos = tryParseInt(positionStr);

            String totalTimeRaw = getTagValue(driverEl, "FinishTime");
            if (totalTimeRaw == null) {
                totalTimeRaw = getTagValue(driverEl, "TotalTime");
            }
            String totalTime = LapTimeHelper.formatSecondsStringToLapTime(totalTimeRaw);

            String bestLapRaw = getTagValue(driverEl, "BestLapTime");
            if (bestLapRaw == null) {
                bestLapRaw = getTagValue(driverEl, "BestLap");
            }
            String bestLap = LapTimeHelper.formatSecondsStringToLapTime(bestLapRaw);

            String category = getTagValue(driverEl, "CarClass");
            if (category == null) {
                category = getTagValue(driverEl, "CarType");
            }

            String gridPosStr = getTagValue(driverEl, "GridPos");
            boolean isPole = "1".equals(gridPosStr);

            String lapsStr = getTagValue(driverEl, "Laps");
            Integer laps = tryParseInt(lapsStr);

            String finishStatusRaw = getTagValue(driverEl, "FinishStatus");
            String dnfReason = getTagValue(driverEl, "DNFReason");
            String driverStatus = "FINISHED";

            if (finishStatusRaw != null && !finishStatusRaw.trim().isEmpty()) {
                String upper = finishStatusRaw.trim().toUpperCase();
                if (upper.contains("DQ") || upper.contains("DISQUALIFIED")) {
                    driverStatus = "DQ";
                } else if (upper.contains("DNF") || dnfReason != null) {
                    driverStatus = "DNF";
                } else if (upper.contains("DNS")) {
                    driverStatus = "DNS";
                }
            } else if (dnfReason != null && !dnfReason.trim().isEmpty()) {
                driverStatus = "DNF";
            }

            Driver matched = DriverMatchingHelper.matchDriver(name, null, carNumber, allDrivers);
            UUID resolvedDriverId = matched != null ? matched.getId() : null;
            String resolvedName = matched != null ? matched.getName() : name;
            String resolvedCarNumber = carNumber != null ? carNumber : (matched != null ? matched.getCarNumber() : "");

            items.add(RaceResultBulkItemRequest.builder()
                    .driverId(resolvedDriverId)
                    .driverName(resolvedName)
                    .carNumber(resolvedCarNumber)
                    .category(category)
                    .finishOrder(finishPos != null ? finishPos : (i + 1))
                    .totalTime(totalTime)
                    .bestLapTime(bestLap)
                    .lapsCompleted(laps)
                    .penaltiesSeconds(0)
                    .fastestLap(false)
                    .polePosition(isPole)
                    .status(driverStatus)
                    .build());
        }
        return items;
    }

    private String getTagValue(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        return (nl != null && nl.getLength() > 0) ? nl.item(0).getTextContent().trim() : null;
    }

    private Integer tryParseInt(String s) {
        if (s == null) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }
}
