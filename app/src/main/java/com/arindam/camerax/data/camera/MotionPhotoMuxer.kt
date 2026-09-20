package com.arindam.camerax.data.camera

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Data: muxes a still JPEG and an MP4 clip into an Android Motion Photo v1 file
 * (JPEG primary + XMP + appended video). Call via [com.arindam.camerax.domain.usecase.CapturePhoto]
 * / [CameraSession] — not from Compose or click handlers (runs on the IO dispatcher).
 *
 * The JPEG is streamed through rather than loaded fully into memory, so large still files
 * (high-res, RAW companion JPEGs) do not cause heap pressure.
 */
object MotionPhotoMuxer {

    fun mux(stillJpeg: File, videoMp4: File, output: File): File {
        val videoLength = videoMp4.length().toInt()
        val xmpSegment = buildXmpSegment(videoLength)
        val insertOffset = findXmpInsertOffset(stillJpeg)

        output.outputStream().buffered().use { outStream ->
            stillJpeg.inputStream().buffered().use { inStream ->
                // Copy JPEG bytes before the XMP insertion point.
                copyExact(inStream, outStream, insertOffset)

                // Write the XMP APP1 segment.
                outStream.write(xmpSegment)

                // Copy the rest of the JPEG.
                inStream.copyTo(outStream)
            }
            // Append the video.
            videoMp4.inputStream().buffered().use { inStream ->
                inStream.copyTo(outStream)
            }
        }
        return output
    }

    fun isMotionPhoto(file: File): Boolean {
        if (!file.extension.equals("jpg", ignoreCase = true) &&
            !file.extension.equals("jpeg", ignoreCase = true)
        ) return false
        val headerSize = minOf(file.length(), 64_000L).toInt()
        if (headerSize <= 0) return false
        val header = ByteArray(headerSize)
        file.inputStream().use { input ->
            var read = 0
            while (read < headerSize) {
                val count = input.read(header, read, headerSize - read)
                if (count <= 0) break
                read += count
            }
        }
        // Require Motion Photo XMP. Trailing bytes after EOI alone are not enough —
        // JPEG Ultra HDR stores a gain map after the primary image and would false-positive.
        if (!String(header, Charsets.ISO_8859_1).contains("Camera:MotionPhoto")) return false
        val video = videoOffset(file) ?: return false
        return video < file.length()
    }

    fun extractVideo(file: File, output: File): File? {
        val offset = videoOffset(file) ?: return null
        file.inputStream().use { input ->
            input.skip(offset)
            output.outputStream().use { input.copyTo(it) }
        }
        return output.takeIf { it.length() > 0 }
    }

    /**
     * Offset of the MP4 appended after the JPEG EOI. Streams the file so a large motion photo
     * cannot OOM the process.
     */
    internal fun videoOffset(file: File): Long? {
        val eoi = jpegEndOffset(file) ?: return null
        if (file.length() - eoi < 8) return null
        return eoi
    }

    private fun jpegEndOffset(file: File): Long? {
        file.inputStream().buffered(64 * 1024).use { input ->
            if (input.read() != 0xFF || input.read() != 0xD8) return null
            var offset = 2L
            while (true) {
                var value = input.read()
                if (value < 0) return null
                offset++
                if (value != 0xFF) continue
                do {
                    value = input.read()
                    if (value < 0) return null
                    offset++
                } while (value == 0xFF)
                when (value) {
                    0xD9 -> return offset
                    0xDA -> {
                        var previous = -1
                        while (true) {
                            val current = input.read()
                            if (current < 0) return null
                            offset++
                            if (previous == 0xFF && current == 0xD9) return offset
                            previous = current
                        }
                    }
                    0x00, 0x01 -> continue
                    in 0xD0..0xD7 -> continue
                    else -> {
                        val lengthHi = input.read()
                        val lengthLo = input.read()
                        if (lengthHi < 0 || lengthLo < 0) return null
                        offset += 2
                        val payload = ((lengthHi shl 8) or lengthLo) - 2
                        if (payload < 0) return null
                        var remaining = payload.toLong()
                        while (remaining > 0) {
                            val skipped = input.skip(remaining)
                            if (skipped <= 0) return null
                            remaining -= skipped
                            offset += skipped
                        }
                    }
                }
            }
        }
    }

    /**
     * Scans the JPEG file to find the byte offset where the XMP APP1 segment should be
     * inserted, without loading the entire file into memory.
     */
    private fun findXmpInsertOffset(file: File): Long {
        file.inputStream().buffered().use { input ->
            if (input.read() != 0xFF || input.read() != 0xD8) return 2L
            var offset = 2L
            while (true) {
                val b1 = input.read()
                if (b1 < 0) return offset
                if (b1 != 0xFF) {
                    offset++
                    continue
                }
                val marker = input.read()
                if (marker < 0) return offset
                // SOS or EOI: insert before here.
                if (marker == 0xDA || marker == 0xD9) return offset
                // Standalone markers (RST, TEM).
                if (marker == 0x01 || marker in 0xD0..0xD7) {
                    offset += 2
                    continue
                }
                val lengthHi = input.read()
                val lengthLo = input.read()
                if (lengthHi < 0 || lengthLo < 0) return offset
                val segmentLength = ((lengthHi shl 8) or lengthLo)
                // APP0 (JFIF) and APP1 (Exif/XMP): skip past and insert after.
                if (marker != 0xE0 && marker != 0xE1) return offset
                val payload = segmentLength - 2
                if (payload > 0) {
                    var remaining = payload.toLong()
                    while (remaining > 0) {
                        val skipped = input.skip(remaining)
                        if (skipped <= 0) return offset
                        remaining -= skipped
                    }
                }
                offset += 2 + segmentLength
            }
        }
    }

    /**
     * Copies exactly [count] bytes from [input] to [output]. Throws if the input
     * is exhausted before [count] bytes are read.
     */
    private fun copyExact(input: InputStream, output: OutputStream, count: Long) {
        val buffer = ByteArray(8192)
        var remaining = count
        while (remaining > 0) {
            val toRead = minOf(remaining, buffer.size.toLong()).toInt()
            val read = input.read(buffer, 0, toRead)
            if (read <= 0) throw IllegalStateException("Unexpected end of JPEG at offset ${count - remaining}")
            output.write(buffer, 0, read)
            remaining -= read
        }
    }

    /**
     * Builds the raw APP1 segment bytes: [FF E1] [length] [XMP namespace\0] [XMP XML].
     */
    private fun buildXmpSegment(videoLength: Int): ByteArray {
        val xmpXml = xmpPacket(videoLength).toByteArray()
        val namespace = "http://ns.adobe.com/xap/1.0/\u0000".toByteArray()
        val payloadSize = namespace.size + xmpXml.size
        val segmentLength = payloadSize + 2
        val segment = ByteArray(4 + payloadSize)
        segment[0] = 0xFF.toByte()
        segment[1] = 0xE1.toByte()
        segment[2] = (segmentLength shr 8).toByte()
        segment[3] = (segmentLength and 0xFF).toByte()
        System.arraycopy(namespace, 0, segment, 4, namespace.size)
        System.arraycopy(xmpXml, 0, segment, 4 + namespace.size, xmpXml.size)
        return segment
    }

    private fun xmpPacket(videoLength: Int): String = """
        <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
        <x:xmpmeta xmlns:x="adobe:ns:meta/">
          <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
            <rdf:Description rdf:about=""
              xmlns:Camera="http://ns.google.com/photos/1.0/camera/"
              xmlns:Container="http://ns.google.com/photos/1.0/container/"
              xmlns:Item="http://ns.google.com/photos/1.0/container/item/"
              Camera:MotionPhoto="1"
              Camera:MotionPhotoVersion="1"
              Camera:MotionPhotoPresentationTimestampUs="0">
              <Container:Directory>
                <rdf:Seq>
                  <rdf:li rdf:parseType="Resource">
                    <Container:Item Item:Semantic="Primary" Item:Mime="image/jpeg" Item:Length="0" Item:Padding="0"/>
                  </rdf:li>
                  <rdf:li rdf:parseType="Resource">
                    <Container:Item Item:Semantic="MotionPhoto" Item:Mime="video/mp4" Item:Length="$videoLength"/>
                  </rdf:li>
                </rdf:Seq>
              </Container:Directory>
            </rdf:Description>
          </rdf:RDF>
        </x:xmpmeta>
        <?xpacket end="w"?>
    """.trimIndent()
}
