package com.corretrilhos.byvictorjv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;

/** Animated, portrait loading screen inspired by endless-runner game loading art. */
public class LoadingScreenView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final Bitmap logo;
    private final long startTime = SystemClock.uptimeMillis();
    private final float density;
    private final float[] particlesX = new float[26];
    private final float[] particlesY = new float[26];
    private final float[] particlesSpeed = new float[26];
    private final int[] particleColors = {
            Color.rgb(255, 224, 91), Color.rgb(255, 255, 255),
            Color.rgb(78, 234, 255), Color.rgb(255, 139, 65)
    };

    private float width;
    private float height;

    public LoadingScreenView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo_corretrilhos);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        Random random = new Random(244);
        for (int i = 0; i < particlesX.length; i++) {
            particlesX[i] = random.nextFloat();
            particlesY[i] = random.nextFloat();
            particlesSpeed[i] = 0.12f + random.nextFloat() * 0.33f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        float seconds = (SystemClock.uptimeMillis() - startTime) / 1000f;
        float progress = clamp(seconds / 6f, 0f, 1f);

        drawSky(canvas, seconds);
        drawCity(canvas, seconds);
        drawAtmosphere(canvas, seconds);
        drawTrack(canvas, seconds);
        drawLogo(canvas, seconds);
        drawCoins(canvas, seconds);
        drawBrand(canvas);
        drawLoadingBar(canvas, progress, seconds);

        postInvalidateDelayed(16);
    }

    private void drawSky(Canvas canvas, float seconds) {
        paint.setShader(new LinearGradient(0, 0, 0, height,
                new int[]{Color.rgb(3, 116, 202), Color.rgb(15, 190, 222), Color.rgb(95, 225, 227)},
                null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);

        // Soft moving clouds and a bright glow behind the logo.
        paint.setColor(0x44FFFFFF);
        paint.setStyle(Paint.Style.FILL);
        float cloudShift = (seconds * width * 0.035f) % (width * 1.4f);
        drawCloud(canvas, width * 0.16f - cloudShift, height * 0.255f, width * 0.13f);
        drawCloud(canvas, width * 0.82f - cloudShift * 0.72f, height * 0.19f, width * 0.16f);

        paint.setShader(new LinearGradient(width * 0.5f, height * 0.20f,
                width * 0.5f, height * 0.63f,
                new int[]{0x55FFFFFF, 0x00FFFFFF}, null, Shader.TileMode.CLAMP));
        canvas.drawCircle(width * 0.5f, height * 0.40f, width * 0.58f, paint);
        paint.setShader(null);
    }

    private void drawCloud(Canvas canvas, float x, float y, float size) {
        canvas.drawCircle(x, y, size * 0.34f, paint);
        canvas.drawCircle(x + size * 0.30f, y - size * 0.12f, size * 0.43f, paint);
        canvas.drawCircle(x + size * 0.68f, y, size * 0.31f, paint);
        canvas.drawRoundRect(x - size * 0.12f, y, x + size * 0.86f,
                y + size * 0.26f, size * 0.13f, size * 0.13f, paint);
    }

    private void drawCity(Canvas canvas, float seconds) {
        // Distant silhouettes.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x5583DCEB);
        canvas.drawRect(0, height * 0.35f, width * 0.13f, height * 0.72f, paint);
        canvas.drawRect(width * 0.86f, height * 0.29f, width, height * 0.72f, paint);
        canvas.drawRect(width * 0.11f, height * 0.43f, width * 0.23f, height * 0.71f, paint);
        canvas.drawRect(width * 0.76f, height * 0.40f, width * 0.89f, height * 0.70f, paint);

        // Near buildings create the familiar endless-runner street framing.
        path.reset();
        path.moveTo(0, height * 0.24f);
        path.lineTo(width * 0.12f, height * 0.28f);
        path.lineTo(width * 0.20f, height * 0.76f);
        path.lineTo(0, height * 0.80f);
        path.close();
        paint.setColor(Color.rgb(13, 91, 159));
        canvas.drawPath(path, paint);

        path.reset();
        path.moveTo(width, height * 0.20f);
        path.lineTo(width * 0.86f, height * 0.25f);
        path.lineTo(width * 0.77f, height * 0.77f);
        path.lineTo(width, height * 0.82f);
        path.close();
        paint.setColor(Color.rgb(226, 241, 244));
        canvas.drawPath(path, paint);

        // Building edge highlights.
        paint.setColor(0x88FFFFFF);
        paint.setStrokeWidth(Math.max(2f, width * 0.008f));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(width * 0.12f, height * 0.28f, width * 0.20f, height * 0.76f, paint);
        canvas.drawLine(width * 0.86f, height * 0.25f, width * 0.77f, height * 0.77f, paint);
        paint.setStyle(Paint.Style.FILL);

        // Repeated windows slide gently to add motion without distracting from the logo.
        float drift = (seconds * 0.035f) % 0.12f;
        for (int i = 0; i < 5; i++) {
            float y = height * (0.34f + i * 0.075f + drift * 0.12f);
            paint.setColor(0x558FE8F4);
            canvas.drawRoundRect(width * 0.025f, y, width * 0.095f, y + height * 0.032f,
                    width * 0.008f, width * 0.008f, paint);
            canvas.drawRoundRect(width * 0.905f, y - height * 0.045f,
                    width * 0.975f, y - height * 0.013f,
                    width * 0.008f, width * 0.008f, paint);
        }
    }

    private void drawAtmosphere(Canvas canvas, float seconds) {
        for (int i = 0; i < particlesX.length; i++) {
            float y = (particlesY[i] + seconds * particlesSpeed[i] * 0.11f) % 1f;
            float x = particlesX[i] * width + (float) Math.sin(seconds * 1.6f + i) * width * 0.012f;
            float radius = (1.3f + (i % 4) * 0.7f) * density;
            paint.setColor(particleColors[i % particleColors.length]);
            paint.setAlpha(105 + (int) (90 * (0.5f + 0.5f * Math.sin(seconds * 3f + i))));
            canvas.drawCircle(x, y * height * 0.76f, radius, paint);
        }
        paint.setAlpha(255);
    }

    private void drawTrack(Canvas canvas, float seconds) {
        float horizonY = height * 0.665f;
        float centerX = width * 0.5f;
        float bottomY = height * 1.04f;

        path.reset();
        path.moveTo(centerX - width * 0.035f, horizonY);
        path.lineTo(centerX + width * 0.035f, horizonY);
        path.lineTo(width * 1.04f, bottomY);
        path.lineTo(-width * 0.04f, bottomY);
        path.close();
        paint.setShader(new LinearGradient(0, horizonY, 0, height,
                new int[]{Color.rgb(98, 118, 129), Color.rgb(42, 55, 72), Color.rgb(20, 31, 49)},
                null, Shader.TileMode.CLAMP));
        canvas.drawPath(path, paint);
        paint.setShader(null);

        // Track sleepers move toward the viewer to simulate forward running.
        float phase = (seconds * 0.72f) % 1f;
        for (int i = 0; i < 13; i++) {
            float t = (i / 13f + phase) % 1f;
            float perspective = t * t;
            float y = horizonY + (bottomY - horizonY) * perspective;
            float halfWidth = width * (0.045f + 0.53f * perspective);
            float stroke = width * (0.009f + 0.022f * perspective);
            paint.setColor(0xFF24364A);
            paint.setStrokeWidth(stroke * 1.28f);
            canvas.drawLine(centerX - halfWidth, y, centerX + halfWidth, y, paint);
            paint.setColor(0xFFB88B55);
            paint.setStrokeWidth(stroke * 0.60f);
            canvas.drawLine(centerX - halfWidth, y - stroke * 0.14f,
                    centerX + halfWidth, y - stroke * 0.14f, paint);
        }

        // Two polished rails.
        for (int side = -1; side <= 1; side += 2) {
            path.reset();
            path.moveTo(centerX + side * width * 0.021f, horizonY);
            path.cubicTo(centerX + side * width * 0.09f, height * 0.77f,
                    centerX + side * width * 0.30f, height * 0.91f,
                    centerX + side * width * 0.46f, bottomY);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(width * 0.025f);
            paint.setColor(Color.rgb(37, 48, 63));
            canvas.drawPath(path, paint);
            paint.setStrokeWidth(width * 0.010f);
            paint.setColor(Color.rgb(224, 232, 235));
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        // Glowing center guideline.
        paint.setColor(0x66FFD65A);
        paint.setStrokeWidth(width * 0.006f);
        canvas.drawLine(centerX, horizonY, centerX, height, paint);
    }

    private void drawLogo(Canvas canvas, float seconds) {
        float bob = (float) Math.sin(seconds * 2.1f) * height * 0.006f;
        float panelWidth = width * 0.62f;
        float panelHeight = Math.min(height * 0.286f, panelWidth * 1.10f);
        float centerY = height * 0.445f + bob;
        rect.set(width * 0.5f - panelWidth / 2f, centerY - panelHeight / 2f,
                width * 0.5f + panelWidth / 2f, centerY + panelHeight / 2f);

        paint.setShadowLayer(18f * density, 0, 8f * density, 0x66001A39);
        paint.setColor(0xD9062449);
        canvas.drawRoundRect(rect, 22f * density, 22f * density, paint);
        paint.clearShadowLayer();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.2f * density);
        paint.setColor(0xAAFFFFFF);
        canvas.drawRoundRect(rect, 22f * density, 22f * density, paint);
        paint.setStyle(Paint.Style.FILL);

        if (logo != null && !logo.isRecycled()) {
            float maxW = panelWidth * 0.91f;
            float maxH = panelHeight * 0.91f;
            float scale = Math.min(maxW / logo.getWidth(), maxH / logo.getHeight());
            float drawW = logo.getWidth() * scale;
            float drawH = logo.getHeight() * scale;
            RectF imageRect = new RectF(width * 0.5f - drawW / 2f,
                    centerY - drawH / 2f, width * 0.5f + drawW / 2f,
                    centerY + drawH / 2f);
            canvas.save();
            Path clip = new Path();
            clip.addRoundRect(rect.left + 4f * density, rect.top + 4f * density,
                    rect.right - 4f * density, rect.bottom - 4f * density,
                    17f * density, 17f * density, Path.Direction.CW);
            canvas.clipPath(clip);
            canvas.drawBitmap(logo, null, imageRect, paint);
            canvas.restore();
        }
    }

    private void drawCoins(Canvas canvas, float seconds) {
        for (int i = 0; i < 5; i++) {
            float x = width * (0.13f + i * 0.18f)
                    + (float) Math.sin(seconds * 1.8f + i * 1.3f) * width * 0.018f;
            float y = height * (0.29f + (i % 3) * 0.075f)
                    + (float) Math.sin(seconds * 2.5f + i) * height * 0.012f;
            float radius = width * (0.026f + (i % 2) * 0.004f);
            float spin = Math.abs((float) Math.cos(seconds * 2.7f + i));

            paint.setColor(0x55FFD34E);
            canvas.drawCircle(x, y, radius * 1.55f, paint);
            rect.set(x - radius * Math.max(0.18f, spin), y - radius,
                    x + radius * Math.max(0.18f, spin), y + radius);
            paint.setShader(new LinearGradient(x, y - radius, x, y + radius,
                    new int[]{Color.rgb(255, 247, 150), Color.rgb(255, 172, 25), Color.rgb(219, 116, 5)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawOval(rect, paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f * density);
            paint.setColor(0xFFFFF0A1);
            canvas.drawOval(rect, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawBrand(Canvas canvas) {
        float titleSize = width * 0.092f;
        Typeface titleTypeface = Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC);
        paint.setTypeface(titleTypeface);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(titleSize);
        String title = "CORRE TRILHOS";
        while (paint.measureText(title) > width * 0.91f && titleSize > 12f * density) {
            titleSize -= 1f * density;
            paint.setTextSize(titleSize);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f * density);
        paint.setColor(Color.rgb(16, 37, 70));
        paint.setShadowLayer(5f * density, 0, 3f * density, 0xAA00152E);
        canvas.drawText(title, width * 0.5f, height * 0.105f, paint);
        paint.clearShadowLayer();

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, height * 0.06f, 0, height * 0.115f,
                new int[]{Color.rgb(255, 245, 111), Color.rgb(255, 170, 35), Color.rgb(250, 91, 32)},
                null, Shader.TileMode.CLAMP));
        canvas.drawText(title, width * 0.5f, height * 0.105f, paint);
        paint.setShader(null);

        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        paint.setTextSize(width * 0.032f);
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(3f * density, 0, 2f * density, 0x99001A36);
        canvas.drawText("BY VICTOR JV STUDIO", width * 0.5f, height * 0.143f, paint);
        paint.clearShadowLayer();
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawLoadingBar(Canvas canvas, float progress, float seconds) {
        float barWidth = width * 0.76f;
        float barHeight = Math.max(8f * density, height * 0.014f);
        float left = (width - barWidth) * 0.5f;
        float top = height * 0.925f;

        paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        paint.setTextSize(width * 0.046f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(4f * density, 0, 2f * density, 0xAA00162E);
        canvas.drawText("CARREGANDO...", width * 0.5f, top - height * 0.028f, paint);
        paint.clearShadowLayer();

        rect.set(left, top, left + barWidth, top + barHeight);
        paint.setColor(0xDD092949);
        canvas.drawRoundRect(rect, barHeight, barHeight, paint);

        rect.set(left + 2f * density, top + 2f * density,
                left + 2f * density + (barWidth - 4f * density) * progress,
                top + barHeight - 2f * density);
        paint.setShader(new LinearGradient(left, top, left + barWidth, top + barHeight,
                new int[]{Color.rgb(255, 222, 74), Color.rgb(255, 143, 28), Color.rgb(255, 91, 45)},
                null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, barHeight, barHeight, paint);
        paint.setShader(null);

        float shineX = left + 2f * density + (barWidth - 4f * density) * progress;
        paint.setColor(0xBBFFFFFF);
        canvas.drawCircle(Math.min(left + barWidth - barHeight, shineX),
                top + barHeight / 2f, barHeight * 0.72f, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
