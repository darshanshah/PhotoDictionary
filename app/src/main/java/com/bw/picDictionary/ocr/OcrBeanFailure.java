
package com.bw.picDictionary.ocr;

/**
 * Bean class for OCR fail data.
 */
public final class OcrBeanFailure {
    private final long timeRequired;
    private final long timestamp;

    OcrBeanFailure(long timeRequired) {
        this.timeRequired = timeRequired;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimeRequired() {
        return timeRequired;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timeRequired + " " + timestamp;
    }
}
