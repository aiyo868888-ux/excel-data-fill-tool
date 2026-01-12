"""
音效生成器 - 核心模块
使用 NumPy 和 SciPy 实时生成各种环境音效
"""

import numpy as np
import soundfile as sf
from scipy import signal
import os


class SoundGenerator:
    """音效生成器核心类"""

    def __init__(self, sample_rate=44100):
        """
        初始化音效生成器

        Args:
            sample_rate: 采样率，默认 44100Hz
        """
        self.sample_rate = sample_rate

    def _apply_fade(self, audio, fade_in_samples, fade_out_samples):
        """
        应用淡入淡出效果

        Args:
            audio: 音频数据
            fade_in_samples: 淡入采样数
            fade_out_samples: 淡出采样数

        Returns:
            处理后的音频数据
        """
        result = audio.copy()
        length = len(audio)

        # 淡入
        if fade_in_samples > 0:
            fade_in = np.linspace(0, 1, min(fade_in_samples, length))
            result[:len(fade_in)] *= fade_in

        # 淡出
        if fade_out_samples > 0:
            fade_out = np.linspace(1, 0, min(fade_out_samples, length))
            result[-len(fade_out):] *= fade_out

        return result

    def generate_white_noise(self, duration=10, volume=0.7, fade_in=0.5, fade_out=0.5):
        """
        生成白噪声（基础雨声）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            fade_in: 淡入时间（秒）
            fade_out: 淡出时间（秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        audio = np.random.normal(0, 1, samples)
        audio = audio * volume / np.max(np.abs(audio))
        audio = self._apply_fade(audio, int(fade_in * self.sample_rate), int(fade_out * self.sample_rate))
        return audio

    def generate_pink_noise(self, duration=10, volume=0.7, fade_in=0.5, fade_out=0.5):
        """
        生成粉红噪声（风声基础）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            fade_in: 淡入时间（秒）
            fade_out: 淡出时间（秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)

        # 使用 Voss-McCartney 算法生成粉红噪声
        white = np.random.normal(0, 1, samples)
        pink = np.zeros(samples)

        # 多个八度的叠加
        b = [0, 0, 0, 0, 0, 0, 0]
        for i in range(samples):
            white_sample = white[i]
            for j in range(len(b)):
                if np.random.random() < 0.5:
                    b[j] = white_sample / (j + 1)
                pink[i] += b[j]

        # 归一化
        pink = pink * volume / np.max(np.abs(pink))
        pink = self._apply_fade(pink, int(fade_in * self.sample_rate), int(fade_out * self.sample_rate))
        return pink

    def generate_brown_noise(self, duration=10, volume=0.7, fade_in=0.5, fade_out=0.5):
        """
        生成布朗噪声（雷声基础）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            fade_in: 淡入时间（秒）
            fade_out: 淡出时间（秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)

        # 布朗噪声是白噪声的积分
        white = np.random.normal(0, 1, samples)
        brown = np.cumsum(white)

        # 归一化
        brown = brown * volume / np.max(np.abs(brown))
        brown = self._apply_fade(brown, int(fade_in * self.sample_rate), int(fade_out * self.sample_rate))
        return brown

    def generate_rain(self, duration=10, volume=0.7, intensity=0.5, fade_in=0.5, fade_out=0.5):
        """
        生成雨声（白噪声 + 高通滤波）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            intensity: 雨强度 (0.0-1.0)，影响高频成分
            fade_in: 淡入时间（秒）
            fade_out: 淡出时间（秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)

        # 生成白噪声
        white = np.random.normal(0, 1, samples)

        # 高通滤波器（滤除低频，保留高频雨滴声）
        high_cutoff = 1000 + (intensity * 3000)  # 1000-4000Hz
        nyquist = self.sample_rate / 2
        high = high_cutoff / nyquist
        b, a = signal.butter(4, high, btype='high')
        filtered = signal.filtfilt(b, a, white)

        # 归一化
        audio = filtered * volume / np.max(np.abs(filtered))
        audio = self._apply_fade(audio, int(fade_in * self.sample_rate), int(fade_out * self.sample_rate))
        return audio

    def generate_wind(self, duration=10, volume=0.7, speed=1.0, fade_in=0.5, fade_out=0.5):
        """
        生成风声（粉红噪声 + 低频调制）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            speed: 风速 (0.5-2.0)，影响调制频率
            fade_in: 淡入时间（秒）
            fade_out: 淡出时间（秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        time = np.linspace(0, duration, samples)

        # 生成粉红噪声
        pink = self.generate_pink_noise(duration, 1.0, 0, 0)

        # 低频调制（模拟风的起伏）
        modulation_freq = 0.1 * speed  # 0.05-0.2Hz
        modulation = 0.5 + 0.5 * np.sin(2 * np.pi * modulation_freq * time)
        modulation = np.interp(time, np.linspace(0, duration, len(modulation)), modulation)

        # 应用调制
        audio = pink * modulation

        # 低通滤波使声音更柔和
        low_cutoff = 2000 / speed
        nyquist = self.sample_rate / 2
        low = low_cutoff / nyquist
        b, a = signal.butter(4, low, btype='low')
        filtered = signal.filtfilt(b, a, audio)

        # 归一化
        audio = filtered * volume / np.max(np.abs(filtered))
        audio = self._apply_fade(audio, int(fade_in * self.sample_rate), int(fade_out * self.sample_rate))
        return audio

    def generate_thunder(self, duration=5, volume=0.9, fade_in=0.1, fade_out=2.0):
        """
        生成雷声（布朗噪声 + 频率扫描）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            fade_in: 淡入时间（秒）
            fade_out: 淡出时间（秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        time = np.linspace(0, duration, samples)

        # 生成布朗噪声
        brown = self.generate_brown_noise(duration, 1.0, 0, 0)

        # 频率扫描（从高频到低频）
        sweep_freq = 100 * np.exp(-3 * time / duration)  # 100Hz -> 5Hz
        oscillator = np.sin(2 * np.pi * np.cumsum(sweep_freq) / self.sample_rate)

        # 混合布朗噪声和振荡器
        audio = 0.7 * brown + 0.3 * oscillator

        # 低通滤波
        low_cutoff = 500
        nyquist = self.sample_rate / 2
        low = low_cutoff / nyquist
        b, a = signal.butter(4, low, btype='low')
        filtered = signal.filtfilt(b, a, audio)

        # 归一化
        audio = filtered * volume / np.max(np.abs(filtered))
        audio = self._apply_fade(audio, int(fade_in * self.sample_rate), int(fade_out * self.sample_rate))
        return audio

    def generate_waves(self, duration=10, volume=0.7, frequency=0.1, fade_in=0.5, fade_out=0.5):
        """
        生成海浪声（粉红噪声 + 低频振荡）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            frequency: 海浪频率 (0.05-0.2Hz)
            fade_in: 淡入时间（秒）
            fade_out: 淡出时间（秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        time = np.linspace(0, duration, samples)

        # 生成粉红噪声
        pink = self.generate_pink_noise(duration, 1.0, 0, 0)

        # 低频振荡（模拟海浪起伏）
        modulation = 0.3 + 0.7 * (0.5 + 0.5 * np.sin(2 * np.pi * frequency * time))

        # 应用调制
        audio = pink * modulation

        # 低通滤波
        low_cutoff = 1500
        nyquist = self.sample_rate / 2
        low = low_cutoff / nyquist
        b, a = signal.butter(4, low, btype='low')
        filtered = signal.filtfilt(b, a, audio)

        # 归一化
        audio = filtered * volume / np.max(np.abs(filtered))
        audio = self._apply_fade(audio, int(fade_in * self.sample_rate), int(fade_out * self.sample_rate))
        return audio

    def generate_forest(self, duration=10, volume=0.6, fade_in=1.0, fade_out=1.0):
        """
        生成森林环境音效（风声 + 随机鸟鸣）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            fade_in: 淡入时间（秒）
            fade_out: 淡出时间（秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        time = np.linspace(0, duration, samples)

        # 风声背景（粉红噪声）
        wind = self.generate_wind(duration, volume * 0.5, 0.8, 0, 0)

        # 鸟鸣（正弦波短促音）
        birds = np.zeros(samples)
        num_birds = int(duration * 2)  # 每秒2次鸟鸣

        for _ in range(num_birds):
            start = np.random.randint(0, max(1, samples - 10000))
            bird_duration = np.random.randint(500, 2000)
            end = min(start + bird_duration, samples)

            # 鸟鸣频率（2000-4000Hz）
            freq = np.random.uniform(2000, 4000)
            bird_time = np.linspace(0, (end - start) / self.sample_rate, end - start)

            # 频率调制（颤音）
            modulation = 1 + 0.1 * np.sin(2 * np.pi * 10 * bird_time)
            bird_sound = 0.1 * np.sin(2 * np.pi * freq * np.cumsum(modulation) / self.sample_rate)

            # 淡入淡出
            fade_len = min(500, len(bird_sound) // 2)
            bird_sound[:fade_len] *= np.linspace(0, 1, fade_len)
            bird_sound[-fade_len:] *= np.linspace(1, 0, fade_len)

            birds[start:end] += bird_sound

        # 混合
        audio = 0.7 * wind + 0.3 * birds

        # 归一化
        audio = audio * volume / np.max(np.abs(audio))
        audio = self._apply_fade(audio, int(fade_in * self.sample_rate), int(fade_out * self.sample_rate))
        return audio

    def generate_door_open(self, duration=2, volume=0.7):
        """
        生成开门声（吱呀声+摩擦声）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        time = np.linspace(0, duration, samples)

        # 摩擦噪声（高频）
        friction = np.random.normal(0, 0.3, samples)

        # 吱呀声（频率调制）
        base_freq = 800
        mod_freq = 3
        creak = np.sin(2 * np.pi * (base_freq + 200 * np.sin(2 * np.pi * mod_freq * time)) * time)

        # 包络（开门声音逐渐增大然后消失）
        envelope = np.concatenate([
            np.linspace(0, 1, int(samples * 0.3)),  # 快速上升
            np.ones(int(samples * 0.4)),          # 保持
            np.linspace(1, 0, int(samples * 0.3))  # 消失
        ])

        # 混合并归一化
        audio = (0.6 * friction + 0.4 * creak) * envelope * volume

        return audio

    def generate_door_close(self, duration=1, volume=0.8, door_type='wood'):
        """
        生成关门声（撞击声+震动）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            door_type: 门类型 ('wood'木门, 'metal'铁门, 'glass'玻璃门)

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        time = np.linspace(0, duration, samples)

        # 撞击声
        if door_type == 'wood':
            # 木门：低频撞击
            impact = np.sin(2 * np.pi * 150 * np.exp(-10 * time) * time)
            resonance_freq = 200
        elif door_type == 'metal':
            # 铁门：金属撞击声
            impact = np.sin(2 * np.pi * 300 * np.exp(-15 * time) * time)
            resonance_freq = 500
        else:  # glass
            # 玻璃门：清脆撞击
            impact = np.sin(2 * np.pi * 500 * np.exp(-20 * time) * time)
            resonance_freq = 800

        # 震动衰减
        resonance = 0.5 * np.sin(2 * np.pi * resonance_freq * time) * np.exp(-5 * time)

        # 摩擦声（关门时的滑动）
        friction = np.random.normal(0, 0.2, int(samples * 0.3))
        friction = np.pad(friction, (samples - len(friction), 0))

        # 混合
        audio = (0.5 * impact + 0.3 * resonance + 0.2 * friction) * volume

        # 归一化
        audio = audio / np.max(np.abs(audio)) * volume

        return audio

    def generate_dog_bark(self, duration=1, volume=0.8, size='medium'):
        """
        生成狗叫声（汪汪）

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            size: 狗大小 ('small'小型, 'medium'中型, 'large'大型)

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        time = np.linspace(0, duration, samples)

        # 根据大小设置频率
        if size == 'small':
            base_freq = 600  # 小狗：高频
            formant_freq = 1200
        elif size == 'large':
            base_freq = 200  # 大狗：低频
            formant_freq = 400
        else:  # medium
            base_freq = 400  # 中型狗
            formant_freq = 800

        # 生成2-3声叫
        num_barks = np.random.randint(2, 4)
        bark_samples = samples // num_barks

        audio = np.zeros(samples)

        for i in range(num_barks):
            start = i * bark_samples
            end = min(start + bark_samples, samples)
            bark_len = end - start

            if bark_len <= 0:
                continue

            # 单声叫的波形
            bark_time = np.linspace(0, bark_len / self.sample_rate, bark_len)

            # 频率调制（叫声的音调变化）
            freq_mod = base_freq * (1 + 0.3 * np.exp(-20 * bark_time))

            # 基础波形
            fundamental = np.sin(2 * np.pi * np.cumsum(freq_mod) / self.sample_rate)

            # 共振峰（声音的音色）
            formant = 0.5 * np.sin(2 * np.pi * formant_freq * bark_time)

            # 包络（快速上升，较慢衰减）
            attack = int(bark_len * 0.1)
            decay = bark_len - attack
            envelope = np.concatenate([
                np.linspace(0, 1, attack),
                np.exp(-3 * np.linspace(0, 3, decay))
            ])

            # 单声叫
            single_bark = (fundamental + formant) * envelope * 0.5

            # 添加到音频
            audio[start:end] += single_bark

        # 添加一些呼吸噪声
        noise = np.random.normal(0, 0.05, samples)
        audio += noise

        # 归一化
        audio = audio / np.max(np.abs(audio)) * volume

        return audio

    def generate_footsteps(self, duration=5, volume=0.6, surface='wood', step_rate=1.0):
        """
        生成脚步声

        Args:
            duration: 时长（秒）
            volume: 音量 (0.0-1.0)
            surface: 地面类型 ('wood'木地板, 'concrete'水泥地, 'grass'草地)
            step_rate: 步频（步/秒）

        Returns:
            numpy array: 音频数据
        """
        samples = int(duration * self.sample_rate)
        audio = np.zeros(samples)

        # 计算脚步数量
        num_steps = int(duration * step_rate * 2)  # 左右脚
        step_interval = samples / num_steps

        for i in range(num_steps):
            step_pos = int(i * step_interval)

            if step_pos >= samples - 1000:
                break

            # 脚步声长度
            step_len = np.random.randint(500, 1500)
            step_end = min(step_pos + step_len, samples)

            if surface == 'wood':
                # 木地板：清脆的撞击声
                step_sound = np.random.normal(0, 1, step_end - step_pos)
                step_sound *= np.linspace(1, 0, step_end - step_pos)  # 衰减
                step_sound *= 0.8

                # 添加一些低频共振
                low_freq = 0.3 * np.sin(2 * np.pi * 100 * np.linspace(0, step_len / self.sample_rate, step_end - step_pos))
                step_sound += low_freq

            elif surface == 'concrete':
                # 水泥地：坚硬的撞击
                step_sound = np.random.normal(0, 1, step_end - step_pos)
                step_sound *= np.linspace(1, 0, step_end - step_pos) ** 0.5  # 较慢衰减
                step_sound *= 1.0

            else:  # grass
                # 草地：柔和的摩擦声
                step_sound = np.random.normal(0, 0.5, step_end - step_pos)
                step_sound *= np.linspace(1, 0, step_end - step_pos) ** 2  # 快速衰减
                step_sound *= 0.6

            # 添加到音频
            audio[step_pos:step_end] += step_sound

        # 归一化
        audio = audio / np.max(np.abs(audio)) * volume

        return audio

    def save_wav(self, audio_data, filename):
        """
        保存音频为 WAV 文件

        Args:
            audio_data: 音频数据
            filename: 输出文件名

        Returns:
            保存的文件路径
        """
        # 确保文件扩展名
        if not filename.endswith('.wav'):
            filename += '.wav'

        # 确保目录存在
        os.makedirs(os.path.dirname(filename), exist_ok=True)

        # 保存文件
        sf.write(filename, audio_data, self.sample_rate)

        return filename

    def mix_sounds(self, sound_files, volumes, output_file):
        """
        混合多个音效文件

        Args:
            sound_files: 音效文件列表
            volumes: 对应的音量列表 (0.0-1.0)
            output_file: 输出文件路径

        Returns:
            混合后的文件路径
        """
        if len(sound_files) != len(volumes):
            raise ValueError("音效文件数量和音量数量必须相同")

        if not sound_files:
            raise ValueError("至少需要一个音效文件")

        # 读取所有音效
        sounds = []
        max_length = 0

        for file_path in sound_files:
            audio, sr = sf.read(file_path)
            if sr != self.sample_rate:
                raise ValueError(f"采样率不匹配：{file_path}")

            if len(audio.shape) == 1:
                audio = audio.reshape(-1, 1)

            sounds.append(audio)
            max_length = max(max_length, len(audio))

        # 填充到相同长度
        padded_sounds = []
        for sound in sounds:
            if len(sound) < max_length:
                padded = np.zeros((max_length, sound.shape[1]))
                padded[:len(sound)] = sound
                padded_sounds.append(padded)
            else:
                padded_sounds.append(sound)

        # 混合
        mixed = np.zeros_like(padded_sounds[0])
        for sound, volume in zip(padded_sounds, volumes):
            mixed += sound * volume

        # 归一化（防止削波）
        max_val = np.max(np.abs(mixed))
        if max_val > 0:
            mixed = mixed / max_val * 0.9

        # 保存
        sf.write(output_file, mixed, self.sample_rate)

        return output_file
