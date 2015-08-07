
package com.bw.picDictionary.ocr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.bw.picDictionary.ocr.camera.CameraManager;

import java.util.List;

public final class Viewfinder extends View {

    private static final boolean DRAW_REGION_BOXES = false;
    private static final boolean DRAW_TEXTLINE_BOXES = true;
    private static final boolean DRAW_WORD_BOXES = true;
    private static final boolean DRAW_TRANSPARENT_WORD_BACKGROUNDS = false;
    private static final boolean DRAW_CHARACTER_BOXES = false;
    private static final boolean DRAW_WORD_TEXT = false;
    private static final boolean DRAW_CHARACTER_TEXT = false;
    private final Paint paint;
    private final int maskColor;
    private final int frameColor;
    private final int cornerColor;
    private CameraManager cameraManager;
    private OcrBeanText resultText;
    private String[] words;
    private List<Rect> wordBoundingBoxes;
    private List<Rect> characterBoundingBoxes;
    private List<Rect> textlineBoundingBoxes;
    private List<Rect> regionBoundingBoxes;
    private Rect previewFrame;
    private Rect rect;
    public Viewfinder(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        frameColor = resources.getColor(R.color.viewfinder_frame);
        cornerColor = resources.getColor(R.color.viewfinder_corners);
        previewFrame = new Rect();
        rect = new Rect();
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressWarnings("unused")
    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = cameraManager.getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultText != null) {

            Point bitmapSize = resultText.getBitmapDimensions();
            previewFrame = cameraManager.getFramingRectInPreview();
            if (bitmapSize.x == previewFrame.width() && bitmapSize.y == previewFrame.height()) {


                float scaleX = frame.width() / (float) previewFrame.width();
                float scaleY = frame.height() / (float) previewFrame.height();

                if (DRAW_REGION_BOXES) {
                    regionBoundingBoxes = resultText.getRegionBoundingBoxes();
                    for (int i = 0; i < regionBoundingBoxes.size(); i++) {
                        paint.setAlpha(0xA0);
                        paint.setColor(Color.MAGENTA);
                        paint.setStyle(Style.STROKE);
                        paint.setStrokeWidth(1);
                        rect = regionBoundingBoxes.get(i);
                        canvas.drawRect(frame.left + rect.left * scaleX,
                                frame.top + rect.top * scaleY,
                                frame.left + rect.right * scaleX,
                                frame.top + rect.bottom * scaleY, paint);
                    }
                }

                if (DRAW_TEXTLINE_BOXES) {
                    textlineBoundingBoxes = resultText.getTextlineBoundingBoxes();
                    paint.setAlpha(0xA0);
                    paint.setColor(Color.RED);
                    paint.setStyle(Style.STROKE);
                    paint.setStrokeWidth(1);
                    for (int i = 0; i < textlineBoundingBoxes.size(); i++) {
                        rect = textlineBoundingBoxes.get(i);
                        canvas.drawRect(frame.left + rect.left * scaleX,
                                frame.top + rect.top * scaleY,
                                frame.left + rect.right * scaleX,
                                frame.top + rect.bottom * scaleY, paint);
                    }
                }

                if (DRAW_WORD_BOXES || DRAW_WORD_TEXT) {
                    wordBoundingBoxes = resultText.getWordBoundingBoxes();

                }

                if (DRAW_WORD_TEXT) {
                    words = resultText.getText().replace("\n", " ").split(" ");
                    int[] wordConfidences = resultText.getWordConfidences();
                    for (int i = 0; i < wordBoundingBoxes.size(); i++) {
                        boolean isWordBlank = true;
                        try {
                            if (!words[i].equals("")) {
                                isWordBlank = false;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }

                        if (!isWordBlank) {
                            rect = wordBoundingBoxes.get(i);
                            paint.setColor(Color.WHITE);
                            paint.setStyle(Style.FILL);
                            if (DRAW_TRANSPARENT_WORD_BACKGROUNDS) {
                                paint.setAlpha(wordConfidences[i] * 255 / 100);
                            } else {
                                paint.setAlpha(255);
                            }
                            canvas.drawRect(frame.left + rect.left * scaleX,
                                    frame.top + rect.top * scaleY,
                                    frame.left + rect.right * scaleX,
                                    frame.top + rect.bottom * scaleY, paint);


                            paint.setColor(Color.BLACK);
                            paint.setAlpha(0xFF);
                            paint.setAntiAlias(true);
                            paint.setTextAlign(Align.LEFT);

                            paint.setTextSize(100);
                            paint.setTextScaleX(1.0f);
                            Rect bounds = new Rect();
                            paint.getTextBounds(words[i], 0, words[i].length(), bounds);
                            int h = bounds.bottom - bounds.top;
                            float size = (((float) (rect.height()) / h) * 100f);
                            paint.setTextSize(size);
                            paint.setTextScaleX(1.0f);
                            paint.getTextBounds(words[i], 0, words[i].length(), bounds);
                            int w = bounds.right - bounds.left;
                            int text_h = bounds.bottom - bounds.top;
                            int baseline = bounds.bottom + ((rect.height() - text_h) / 2);
                            float xscale = ((float) (rect.width())) / w;
                            paint.setTextScaleX(xscale);
                            canvas.drawText(words[i], frame.left + rect.left * scaleX, frame.top + rect.bottom * scaleY - baseline, paint);
                        }

                    }
                }

                if (DRAW_WORD_BOXES) {
                    paint.setAlpha(0xA0);
                    paint.setColor(0xFF00CCFF);
                    paint.setStyle(Style.STROKE);
                    paint.setStrokeWidth(1);
                    for (int i = 0; i < wordBoundingBoxes.size(); i++) {
                        rect = wordBoundingBoxes.get(i);
                        canvas.drawRect(
                                frame.left + rect.left * scaleX,
                                frame.top + rect.top * scaleY,
                                frame.left + rect.right * scaleX,
                                frame.top + rect.bottom * scaleY, paint);
                    }
                }

                if (DRAW_CHARACTER_BOXES || DRAW_CHARACTER_TEXT) {
                    characterBoundingBoxes = resultText.getCharacterBoundingBoxes();
                }

                if (DRAW_CHARACTER_BOXES) {
                    // Draw bounding boxes around each character
                    paint.setAlpha(0xA0);
                    paint.setColor(0xFF00FF00);
                    paint.setStyle(Style.STROKE);
                    paint.setStrokeWidth(1);
                    for (int c = 0; c < characterBoundingBoxes.size(); c++) {
                        Rect characterRect = characterBoundingBoxes.get(c);
                        canvas.drawRect(frame.left + characterRect.left * scaleX,
                                frame.top + characterRect.top * scaleY,
                                frame.left + characterRect.right * scaleX,
                                frame.top + characterRect.bottom * scaleY, paint);
                    }
                }

                if (DRAW_CHARACTER_TEXT) {
                    // Draw letters individually
                    for (int i = 0; i < characterBoundingBoxes.size(); i++) {
                        Rect r = characterBoundingBoxes.get(i);

                        // Draw a white background for every letter
                        int meanConfidence = resultText.getMeanConfidence();
                        paint.setColor(Color.WHITE);
                        paint.setAlpha(meanConfidence * (255 / 100));
                        paint.setStyle(Style.FILL);
                        canvas.drawRect(frame.left + r.left * scaleX,
                                frame.top + r.top * scaleY,
                                frame.left + r.right * scaleX,
                                frame.top + r.bottom * scaleY, paint);

                        // Draw each letter, in black
                        paint.setColor(Color.BLACK);
                        paint.setAlpha(0xFF);
                        paint.setAntiAlias(true);
                        paint.setTextAlign(Align.LEFT);
                        String letter = "";
                        try {
                            char c = resultText.getText().replace("\n", "").replace(" ", "").charAt(i);
                            letter = Character.toString(c);

                            if (!letter.equals("-") && !letter.equals("_")) {

                                // Adjust text size to fill rect
                                paint.setTextSize(100);
                                paint.setTextScaleX(1.0f);

                                // ask the paint for the bounding rect if it were to draw this text
                                Rect bounds = new Rect();
                                paint.getTextBounds(letter, 0, letter.length(), bounds);

                                // get the height that would have been produced
                                int h = bounds.bottom - bounds.top;

                                // figure out what textSize setting would create that height of text
                                float size = (((float) (r.height()) / h) * 100f);

                                // and set it into the paint
                                paint.setTextSize(size);

                                // Draw the text as is. We don't really need to set the text scale, because the dimensions
                                // of the Rect should already be suited for drawing our letter.
                                canvas.drawText(letter, frame.left + r.left * scaleX, frame.top + r.bottom * scaleY, paint);
                            }
                        } catch (StringIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
        // Draw a two pixel solid border inside the framing rect
        paint.setAlpha(0);
        paint.setStyle(Style.FILL);
        paint.setColor(frameColor);
        canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
        canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
        canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
        canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);

        // Draw the framing rect corner UI elements
        paint.setColor(cornerColor);
        canvas.drawRect(frame.left - 15, frame.top - 15, frame.left + 15, frame.top, paint);
        canvas.drawRect(frame.left - 15, frame.top, frame.left, frame.top + 15, paint);
        canvas.drawRect(frame.right - 15, frame.top - 15, frame.right + 15, frame.top, paint);
        canvas.drawRect(frame.right, frame.top - 15, frame.right + 15, frame.top + 15, paint);
        canvas.drawRect(frame.left - 15, frame.bottom, frame.left + 15, frame.bottom + 15, paint);
        canvas.drawRect(frame.left - 15, frame.bottom - 15, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right - 15, frame.bottom, frame.right + 15, frame.bottom + 15, paint);
        canvas.drawRect(frame.right, frame.bottom - 15, frame.right + 15, frame.bottom + 15, paint);
    }

    public void drawViewfinder() {
        invalidate();
    }

    /**
     * Nullifies OCR text to remove it at the next onDraw() drawing.
     */
    public void removeResultText() {
        resultText = null;
    }
}
