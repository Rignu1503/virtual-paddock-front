package com.virtual_paddock.backend.infrastructure.helper.simulator;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public final class FileEncodingHelper {

    private FileEncodingHelper() {}

    /**
     * Decodifica un array de bytes de forma universal tolerando UTF-8, Windows-1252 e ISO-8859-1.
     */
    public static String decodeUniversalString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        // 1. Intentar decodificación UTF-8 estricta
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (Exception ignored) {
            // 2. Si contiene bytes de codificaciones tradicionales (ej. Windows-1252 / ISO-8859-1 en rFactor)
            try {
                return new String(bytes, Charset.forName("Windows-1252"));
            } catch (Exception fallbackIgnored) {
                return new String(bytes, StandardCharsets.ISO_8859_1);
            }
        }
    }
}
