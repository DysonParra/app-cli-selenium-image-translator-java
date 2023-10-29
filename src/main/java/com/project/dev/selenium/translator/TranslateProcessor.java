/*
 * @fileoverview    {TranslateProcessor}
 *
 * @version         2.0
 *
 * @author          Dyson Arley Parra Tilano <dysontilano@gmail.com>
 *
 * @copyright       Dyson Parra
 * @see             github.com/DysonParra
 *
 * History
 * @version 1.0     Implementation done.
 * @version 2.0     Documentation added.
 */
package com.project.dev.selenium.translator;

import com.google.common.collect.ImmutableMap;
import com.project.dev.file.generic.FileProcessor;
import com.project.dev.flag.processor.Flag;
import com.project.dev.flag.processor.FlagMap;
import com.project.dev.selenium.generic.SeleniumProcessor;
import com.project.dev.selenium.generic.struct.action.NodeScreenshot;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * TODO: Description of {@code TranslateProcessor}.
 *
 * @author Dyson Parra
 * @since 11
 */
@Data
public class TranslateProcessor {

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private static String screenshotsBaseName = "screenshot";
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private static int translatedQuantity = 0;

    /**
     * TODO: Description of {@code addUrlsToList}.
     *
     * @param line
     * @param list
     * @return
     */
    private static boolean addUrlsToList(String line, List<String> list) {
        if (line.matches("(http://|https://)(lens.google.com).*?"))
            list.add(line);
        return true;
    }

    /**
     * TODO: Description of {@code addUrlToFile}.
     *
     * @param filePath
     * @param imagePath
     * @param url
     */
    public static void addUrlToFile(@NonNull String filePath, @NonNull String imagePath, @NonNull String url) {
        try ( FileOutputStream fos = new FileOutputStream(filePath, true);
                 OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 BufferedWriter writer = new BufferedWriter(osr);) {
            StringBuilder spacesBuilder = new StringBuilder();
            for (int i = 0; i < (111 - imagePath.length()); i++)
                spacesBuilder.append(" ");

            writer.write(new StringBuilder()
                    .append("\"")
                    .append(imagePath)
                    .append("\"")
                    .append(spacesBuilder.toString())
                    .append(" \"")
                    .append(url)
                    .append("\"\n")
                    .toString());

        } catch (Exception e) {
            System.out.println("Could not write in file.");
        }
    }

    /**
     * TODO: Description of {@code getTranslatedImage}.
     *
     * @param driver
     * @param flagsMap
     * @return
     */
    public static boolean getTranslatedImage(@NonNull WebDriver driver, @NonNull Map<String, String> flagsMap) {
        boolean result = true;
        try {
            String outputPath = flagsMap.get("-outputPath");
            String fileName = flagsMap.get("-fileName");
            String subPaths = flagsMap.get("-subPaths");
            String fullOutputPath = (subPaths == null) ? outputPath : outputPath + subPaths;
            String targetLocaleLanguage = flagsMap.get("-targetLocaleLanguage");
            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript(
                    "var imageDiv = document.getElementsByClassName('IDvEJb')[0];"
                    + "var originalImage = document.getElementsByTagName('img')[0];"
                    + "var url = originalImage.src;"
                    + "var img = new Image;"
                    + "img.onload = function() {"
                    + "imageDiv.style.width = img.width + 'px'; imageDiv.style.height = img.height + 'px';"
                    + "};"
                    + "img.src = url;"
            );

            js.executeScript("var body = document.getElementsByTagName('body')[0];"
                    + "body.style['min-width'] ='5000px'; body.style['min-height'] = '5000px';");

            js.executeScript("var headerPanel = document.getElementsByClassName('pGxpHc')[0];"
                    + "headerPanel.remove();");

            js.executeScript("var textActionButton = document.getElementById('text').getElementsByTagName('button')[0];"
                    + "textActionButton.click();");

            js.executeScript("var translateActionButton = document.getElementById('translate').getElementsByTagName('button')[0];"
                    + "translateActionButton.click();");

            WebElement targetButton;
            WebElement selectLanguagePanel;
            targetButton = driver.findElement(By.id("target")).findElement(By.tagName("button"));

            boolean foundLocaleLanguage = false;
            for (int i = 0; i < 2; i++) {
                targetButton.click();
                selectLanguagePanel = driver.findElement(By.className("GSYwFf"));

                if (i == 0) {
                    List<WebElement> languageList = selectLanguagePanel.findElements(By.tagName("li"));
                    languageList.remove(0);
                    languageList.get(0).click();
                } else {
                    List<WebElement> languageList = selectLanguagePanel.findElements(
                            By.xpath("//li[@data-locale='" + targetLocaleLanguage + "']")
                    );
                    if (languageList.size() > 1)
                        languageList.remove(0);
                    if (!languageList.isEmpty()) {
                        languageList.get(0).click();
                        foundLocaleLanguage = true;
                    }
                }
            }

            if (foundLocaleLanguage)
                System.out.println("Target locale language: '" + targetLocaleLanguage + "' found.");
            else {
                result = false;
                String errMessage = "Target locale language: '" + targetLocaleLanguage + "' not found.";
                System.out.println(errMessage);
                throw new Exception(errMessage);
            }
            Thread.sleep(2000);

            js.executeScript("var translatePanel = document.getElementsByClassName('z3qvzf')[0];"
                    + "translatePanel.remove();");

            js.executeScript("var actionPanel = document.getElementsByClassName('SAvApe')[0];"
                    + "actionPanel.remove();");

            js.executeScript("var wordsPanel = document.getElementsByClassName('jXKZBd')[0];"
                    + "wordsPanel.remove();");

            WebElement translatedImageDiv = driver.findElement(By.className("IDvEJb"));
            if (fileName == null)
                NodeScreenshot.getFullNodeScreenshot(driver, translatedImageDiv, fullOutputPath,
                        screenshotsBaseName + "-" + String.format("%03d", ++translatedQuantity));
            else
                NodeScreenshot.getFullNodeScreenshot(driver, translatedImageDiv, fullOutputPath,
                        fileName);
        } catch (Exception e) {
            result = false;
            System.out.println("Error translating image.");
            //e.printStackTrace(System.out);
        }
        return result;
    }

    /**
     * TODO: Description of {@code processImageInGoogleLens}.
     *
     * @param driver
     * @param flagsMap
     * @return
     */
    public static boolean processImageInGoogleLens(@NonNull WebDriver driver, @NonNull Map<String, String> flagsMap) {
        boolean result = true;
        try {
            Integer loadPageTimeOut = Integer.parseInt(flagsMap.get("-loadPageTimeOut"));
            Integer uploadImageTimeOut = Integer.parseInt(flagsMap.get("-uploadImageTimeOut"));
            String inputPath = flagsMap.get("-inputPath");
            String outputPath = flagsMap.get("-outputPath");
            String inputFile = flagsMap.get("-inputFilePath");
            File input = new File(inputFile);
            String fullInputPath = input.getParent();
            String fullOutputPath = fullInputPath.replace(inputPath, outputPath);
            String subPaths = fullInputPath.replace(inputPath, "");

            String fileName = input.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));

            String outputFile = fullOutputPath + "\\" + fileName;
            String inputFileAbs = input.getAbsolutePath();
            flagsMap.put("-fileName", fileName);
            flagsMap.put("-subPaths", subPaths);
            String translatedUrl = "";

            //System.out.println("In Path:        " + inputPath);
            //System.out.println("Out Path:       " + outputPath);
            //System.out.println("SubPaths:       " + subPaths);
            //System.out.println("Full In Path:   " + fullInputPath);
            //System.out.println("Full Out Path:  " + fullOutputPath);
            //System.out.println("Name:           " + fileName);
            //System.out.println("In File:        " + inputFile);
            //System.out.println("Out File:       " + outputFile + ".png");
            //System.out.println("In Abs:         " + inputFileAbs);
            //if (result)
            //    return true;
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("var lensButton = document.getElementById('R5mgy');"
                        + "lensButton.click();");

                WebElement uploadButton = driver.findElement(By.className("DV7the"));
                uploadButton.click();
                Thread.sleep(1000);
                driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(uploadImageTimeOut));

                driver.switchTo().activeElement();
                StringSelection clipboard = new StringSelection(inputFileAbs);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboard, null);
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);

                try {
                    new WebDriverWait(driver, Duration.ofMillis(uploadImageTimeOut))
                            .until((WebDriver webDriver) -> ((JavascriptExecutor) webDriver)
                            .executeScript("var errorPanel = document.getElementsByClassName('alTBQe')[0];"
                                    + "return (errorPanel == undefined && document.readyState == 'complete') "
                                    + "|| errorPanel.style.display != 'none';")
                            );
                    try {
                        driver.findElement(By.className("alTBQe"));
                        String errMessage = "Invalid image format or size more that 20MB.";
                        System.out.println(errMessage);
                        throw new Exception(errMessage);
                    } catch (org.openqa.selenium.NoSuchElementException pageChanged) {
                        File outFile = new File(outputFile);
                        outFile.getParentFile().mkdirs();
                        Thread.sleep(100);
                        result = getTranslatedImage(driver, flagsMap);
                        if (result) {
                            translatedUrl = driver.getCurrentUrl();
                        } else {
                            translatedUrl = "Error translating image.";
                        }
                    }
                } catch (Exception e) {
                    result = false;
                    translatedUrl = "Error uploading image.";
                    System.out.println(translatedUrl);
                    //e.printStackTrace(System.out);
                } finally {
                    driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(loadPageTimeOut));
                }
            } catch (Exception e) {
                translatedUrl = "Error processing image.";
                System.out.println(translatedUrl);
                //e.printStackTrace();
            }
            addUrlToFile(outputPath + "\\" + "output_urls.xml", inputFile, translatedUrl);
        } catch (Exception e) {
            System.out.println("Error getting flags of image.");
            //e.printStackTrace();
        }
        System.out.println("End processing image.");
        System.out.println(result);
        return true;
    }

    /**
     * TODO: Description of {@code processFlags}.
     *
     * @param flags
     * @return
     */
    public static boolean processFlags(Flag[] flags) {
        boolean result = false;

        Map<String, String> flagsMap = FlagMap.convertFlagsArrayToMap(flags);
        String chromeDriverPath = flagsMap.get("-chromeDriverPath");
        String outputPath = flagsMap.get("-outputPath");
        String chromeUserDataDir = System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\User Data";
        String chromeProfileDir = flagsMap.get("-chromeProfileDir");
        String inputPath = flagsMap.get("-inputPath");
        String urlsFilePath = flagsMap.get("-urlsFilePath");
        String targetLocaleLanguage = "es";

        int maxLoadPageTries = 3;
        int delayTimeBeforeRetry = 2000;
        int loadPageTimeOut = 10000;
        int uploadImageTimeOut = 30000;
        int delayTimeBeforeNextPage = 200;

        chromeUserDataDir = FlagMap.validateFlagInMap(flagsMap, "-chromeUserDataDir", chromeUserDataDir, String.class);
        maxLoadPageTries = FlagMap.validateFlagInMap(flagsMap, "-maxLoadPageTries", maxLoadPageTries, Integer.class);
        delayTimeBeforeRetry = FlagMap.validateFlagInMap(flagsMap, "-delayTimeBeforeRetry", delayTimeBeforeRetry, Integer.class);
        loadPageTimeOut = FlagMap.validateFlagInMap(flagsMap, "-loadPageTimeOut", loadPageTimeOut, Integer.class);
        FlagMap.validateFlagInMap(flagsMap, "-uploadImageTimeOut", uploadImageTimeOut, Integer.class);
        FlagMap.validateFlagInMap(flagsMap, "-delayTimeBeforeNextPage", delayTimeBeforeNextPage, Integer.class);
        FlagMap.validateFlagInMap(flagsMap, "-targetLocaleLanguage", targetLocaleLanguage, String.class);

        if (!FileProcessor.validateFile(chromeDriverPath)) {
            System.out.println("Invalid file in flag '-chromeDriverPath'");
            result = false;
        } else if (!FileProcessor.validatePath(outputPath) && !new File(outputPath).mkdirs()) {
            System.out.println("Invalid path in flag '-outputPath'");
            result = false;
        } else if (!FileProcessor.validatePath(chromeUserDataDir)) {
            System.out.println("Invalid path in flag '-chromeUserDataDir'");
            result = false;
        } else if (inputPath != null && !FileProcessor.validatePath(inputPath)) {
            System.out.println("Invalid path in flag '-inputPath'");
            result = false;
        } else if (urlsFilePath != null && !FileProcessor.validateFile(urlsFilePath)) {
            System.out.println("Invalid file in flag '-urlsFilePath'");
            result = false;
        } else {
            List<String> urls = new ArrayList<>();
            List<File> files = new ArrayList<>();
            if (urlsFilePath != null) {
                result = FileProcessor.forEachLine(urlsFilePath, TranslateProcessor::addUrlsToList, urls);
                System.out.println("\nUrls:");
                urls.forEach(url -> System.out.println(url));
                System.out.println("");
            } else if (inputPath != null) {
                FileProcessor.getFiles(new File(inputPath), new String[]{".jpg", ".png", ".webp"}, files);
                System.out.println("\nFiles:");
                files.forEach(file -> System.out.println(file));
                System.out.println("");
            }

            if (!urls.isEmpty() || !files.isEmpty()) {
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                ChromeOptions options = new ChromeOptions();
                options.addArguments("user-data-dir=" + chromeUserDataDir);
                if (flagsMap.get("--notUseIncognito") == null)
                    options.addArguments("--incognito");
                if (chromeProfileDir != null)
                    options.addArguments("--profile-directory=" + chromeProfileDir);

                ChromeDriver driver = new ChromeDriver(options);
                DevTools devTools = driver.getDevTools();
                devTools.createSession();
                devTools.send(new Command<>("Network.enable", ImmutableMap.of()));

                if (inputPath != null) {
                    List<String> uploadPage = Arrays.asList("https://www.google.com.co/search?q=google&tbm=isch");
                    for (File file : files) {
                        flagsMap.put("-inputFilePath", file.toString());
                        result = SeleniumProcessor.forEachPage(driver, uploadPage, maxLoadPageTries,
                                delayTimeBeforeRetry, loadPageTimeOut, TranslateProcessor::processImageInGoogleLens,
                                flagsMap);
                        if (!result)
                            break;
                    }
                } else if (urlsFilePath != null) {
                    result = SeleniumProcessor.forEachPage(driver, urls, maxLoadPageTries,
                            delayTimeBeforeRetry, loadPageTimeOut, TranslateProcessor::getTranslatedImage,
                            flagsMap);
                }
                System.out.println("Finish processing pages...");
                driver.quit();
            }
        }
        return result;
    }

}
