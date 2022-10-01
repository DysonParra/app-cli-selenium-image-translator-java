/*
 * @fileoverview {TranslateProcessor}, se encarga de realizar tareas especificas.
 *
 * @version             1.0
 *
 * @author              Dyson Arley Parra Tilano <dysontilano@gmail.com>
 * Copyright (C) Dyson Parra
 *
 * @history v1.0 --- La implementacion de {TranslateProcessor} fue realizada el 14/08/2022.
 * @dev - La primera version de {TranslateProcessor} fue escrita por Dyson A. Parra T.
 */
package com.project.dev.translator;

import com.google.common.collect.ImmutableMap;
import com.project.dev.flag.processor.Flag;
import com.project.dev.flag.processor.FlagMap;
import com.project.dev.generic.processor.FileProcessor;
import com.project.dev.generic.processor.SeleniumProcessor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;

/**
 * TODO: Definición de {@code TranslateProcessor}.
 *
 * @author Dyson Parra
 * @since 1.8
 */
@Data
public class TranslateProcessor {

    private static String screenshotsBaseName = "screenshot";
    private static int translatedQuantity = 0;

    /**
     * TODO: Definición de {@code addUrlsToList}.
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
     * TODO: Definición de {@code getTranslatedImage}.
     *
     * @param driver
     * @param flagsMap
     * @return
     */
    public static boolean getTranslatedImage(@NonNull WebDriver driver, @NonNull Map<String, String> flagsMap) {
        try {
            String outputPath = flagsMap.get("-outputPath");
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
                System.out.println("Target locale language: '" + targetLocaleLanguage + "' not found.");
                return false;
            }
            Thread.sleep(2000);

            js.executeScript("var translatePanel = document.getElementsByClassName('z3qvzf')[0];"
                    + "translatePanel.remove();");

            js.executeScript("var actionPanel = document.getElementsByClassName('SAvApe')[0];"
                    + "actionPanel.remove();");

            js.executeScript("var wordsPanel = document.getElementsByClassName('jXKZBd')[0];"
                    + "wordsPanel.remove();");

            WebElement translatedImageDiv = driver.findElement(By.className("IDvEJb"));
            SeleniumScreenshot.getFullNodeScreenshot(driver, translatedImageDiv, outputPath, screenshotsBaseName + "-" + String.format("%03d", ++translatedQuantity));
        } catch (Exception e) {
            System.out.println("Error processing page.");
            e.printStackTrace(System.out);
        }
        return true;
    }

    /**
     * TODO: Definición de {@code processFlags}.
     *
     * @param flags
     * @return
     */
    public static boolean processFlags(Flag[] flags) {
        boolean result;

        Map<String, String> flagsMap = FlagMap.convertFlagsArrayToMap(flags);
        String chromeDriverPath = flagsMap.get("-chromeDriverPath");
        String outputPath = flagsMap.get("-outputPath");
        String chromeUserDataDir = System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\User Data";
        String chromeProfileDir = flagsMap.get("-chromeProfileDir");
        String imagesDir = flagsMap.get("-imagesDir");
        String urlsFilePath = flagsMap.get("-urlsFilePath");
        String targetLocaleLanguage = "es";

        int maxLoadPageTries = 3;
        int delayTimeBeforeRetry = 2000;
        int loadPageTimeOut = 10000;
        int delayTimeBeforeNextPage = 200;

        chromeUserDataDir = FlagMap.validateFlagInMap(flagsMap, "-chromeUserDataDir", chromeUserDataDir, String.class);
        maxLoadPageTries = FlagMap.validateFlagInMap(flagsMap, "-maxLoadPageTries", maxLoadPageTries, Integer.class);
        delayTimeBeforeRetry = FlagMap.validateFlagInMap(flagsMap, "-delayTimeBeforeRetry", delayTimeBeforeRetry, Integer.class);
        loadPageTimeOut = FlagMap.validateFlagInMap(flagsMap, "-loadPageTimeOut", loadPageTimeOut, Integer.class);
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
        } else if (imagesDir != null && !FileProcessor.validatePath(imagesDir)) {
            System.out.println("Invalid file in flag '-imagesDir'");
            result = false;
        } else if (urlsFilePath != null && !FileProcessor.validateFile(urlsFilePath)) {
            System.out.println("Invalid file in flag '-urlsFilePath'");
            result = false;
        } else {
            List<String> urls = new ArrayList<>();
            result = FileProcessor.forEachLine(urlsFilePath, TranslateProcessor::addUrlsToList, urls);

            System.out.println("\nUrls:");
            urls.forEach(url -> System.out.println(url));
            System.out.println("");

            if (!urls.isEmpty()) {
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

                result = SeleniumProcessor.forEachPage(driver, urls, maxLoadPageTries,
                        delayTimeBeforeRetry, loadPageTimeOut, TranslateProcessor::getTranslatedImage,
                        flagsMap);

                System.out.println("Finish processing pages...");
                driver.quit();
            }
        }
        return result;
    }

}
