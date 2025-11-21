package io.github.nekosora.api.sound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundEngine {
    private static final ExecutorService soundPool = Executors.newFixedThreadPool(4);
    private static final Logger log = LoggerFactory.getLogger(SoundEngine.class);

    /**
     * 读取resource/audios下的内容并播放 (.wav)
     * @param sound 包含路径和播放设置的Sound对象
     */
    public static void playSound(Sound sound) {
        soundPool.submit(() -> {
            try {
                // 从 Sound 对象获取路径
                InputStream audioStream = SoundEngine.class.getClassLoader()
                        .getResourceAsStream("audios/" + sound.getPath());

                if (audioStream == null) {
                    System.err.println("音频文件未找到: " + sound.getPath());
                    return;
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioStream);
                AudioFormat format = audioInputStream.getFormat();

                DataLine.Info info = new DataLine.Info(Clip.class, format);
                Clip clip = (Clip) AudioSystem.getLine(info);

                clip.open(audioInputStream);

                // 设置音量 (Volume)
                // sound.getVolume() 范围: 0.0 (静音) to 1.0 (最大)
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    if (sound.getVolume() <= 0.0001) {
                        volumeControl.setValue(volumeControl.getMinimum()); // 静音
                    } else {
                        // 将 0.0-1.0 的线性音量转换为对数分贝 (dB)
                        // 公式: 20 * log10(volume)
                        float dB = (float) (Math.log10(sound.getVolume()) * 20.0);
                        // 确保值在控件允许的范围内
                        volumeControl.setValue(Math.max(volumeControl.getMinimum(), Math.min(volumeControl.getMaximum(), dB)));
                    }
                }

                // 设置声相 (Balance)
                // sound.getBalance() 范围: 0.0 (左) - 0.5 (中) - 1.0 (右)
                if (clip.isControlSupported(FloatControl.Type.PAN)) {
                    FloatControl panControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);
                    // 将 0.0~1.0 映射到 API 的 -1.0~1.0
                    // 公式: (balance * 2.0) - 1.0
                    float pan = (float) (sound.getBalance() * 2.0 - 1.0);
                    // 确保值在 -1.0f 到 1.0f 之间
                    pan = Math.max(-1.0f, Math.min(1.0f, pan));
                    panControl.setValue(pan);
                }

                // 设置速度 (Speed) - 这也会同时改变音高 (Pitch)
                // sound.getSpeed() 范围: 1.0 (正常), 1.5 (1.5倍速), 0.8 (0.8倍速)
                if (clip.isControlSupported(FloatControl.Type.SAMPLE_RATE)) {
                    try {
                        FloatControl sampleRateControl = (FloatControl) clip.getControl(FloatControl.Type.SAMPLE_RATE);
                        float originalRate = sampleRateControl.getValue();
                        float newRate = (float) (originalRate * sound.getSpeed());

                        // 确保新速率在允许的范围内
                        newRate = Math.max(sampleRateControl.getMinimum(), Math.min(sampleRateControl.getMaximum(), newRate));

                        sampleRateControl.setValue(newRate);
                    } catch (IllegalArgumentException e) {
                        SoundEngine.log.error("Set sample (Speed) failed: {}", e.getMessage());
                        // 某些音频格式可能不支持动态更改采样率
                    }
                }

                clip.start();

                // 等待播放完成
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 关闭音频引擎
     */
    public static void shutdown() {
        soundPool.shutdown();
    }
}