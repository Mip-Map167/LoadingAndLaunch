import java.awt.Desktop;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Main {
    public static void main(String[] args) {
        String fileURL_music = "https://dl3s5.muzika.fun/aHR0cDovL2YubXAzcG9pc2submV0L21wMy8wMDkvNDkzLzkzMi85NDkzOTMyLm1wMw==";
        String fileURL_picture = "https://upload.wikimedia.org/wikipedia/commons/2/2c/Battle_of_Gaugamela%2C_331_BC_-_Opening_movements.png";
        String saveDirectory = "/Users/oleg/Documents/программирование/системное программирование/10 лаба (запуск и скачивание)";

        try {
            createDirectoryIfNotExists(saveDirectory);
            downloadFileNIO(fileURL_music, saveDirectory);
            downloadFileIO(fileURL_picture, saveDirectory);
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке файла: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void downloadFileNIO(String fileURL_music, String saveDirectory) throws IOException {
        URL url = new URL(fileURL_music);
        String fileName = getFileNameFromURL(url);
        String filePath = saveDirectory + File.separator + fileName;
        System.out.println("\nСохранение аудиофайла \u001B[32m" + fileName + "\u001B[0m по адресу: \u001B[34m" + filePath + "\u001B[0m");

        try (InputStream input = url.openStream()) {
            Files.copy(input, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Файл \u001B[32m" + fileName + "\u001B[0m успешно загружен! Воспроизведение файла...");
        try {
            String musicPath = saveDirectory + File.separator + fileName;
            File audioFile = new File(musicPath);
            if (!audioFile.exists()) {
                System.out.println("Ошибка! Файл " + fileName + " по адресу " + musicPath + " не найден!");
                return;
            }

            // Воспроизведение аудиофайла на macOS:
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(audioFile);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при воспроизведении файла: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void downloadFileIO(String fileURL_picture, String saveDirectory) throws IOException {
        URL url = new URL(fileURL_picture);
        String fileName = getFileNameFromURL(url);
        String filePath = saveDirectory + File.separator + fileName;
        System.out.println("\nЗагрузка изображения \u001B[32m" + fileName + "\u001B[0m в \u001B[34m" + filePath + "\u001B[0m");

        // Разрешение загрузки на macOS:
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
        connection.setRequestProperty("Referer", "https://commons.wikimedia.org/");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
            try {
                File imageFile = new File(filePath);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(imageFile);
                    System.out.println("Файл \u001B[32m" + fileName + "\u001B[0m успешно загружен! Открытие файла...");
                }
            } catch (Exception e) {
                System.err.println("Ошибка открытия файла: " + e.getMessage());
            }
        } else {
            System.out.println("Ошибка загрузки. Код: " + responseCode);
        }
    }

    private static String getFileNameFromURL(URL url) {
        String path = url.getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1);

        // Если имя файла слишком длинное или содержит непонятные символы, создаём нормальное имя:
        if (fileName.length() > 50 || !fileName.contains(".")) {
            // Для аудиофайлов:
            if (url.toString().contains(".mp3") || url.toString().contains("muzika")) {
                return "downloaded_audio.mp3";
            }
            // Для изображений:
            else if (url.toString().contains(".png") || url.toString().contains(".jpg")) {
                return "downloaded_image.png";
            }
            // Общий случай:
            else {
                return "downloaded_file";
            }
        }
        return fileName;
    }

    private static void createDirectoryIfNotExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Создана директория: " + dirPath);
        }
    }
}