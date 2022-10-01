/*
 * @fileoverview {SeleniumScreenshot}, se encarga de realizar tareas especificas.
 *
 * @version             1.0
 *
 * @author              Dyson Arley Parra Tilano <dysontilano@gmail.com>
 * Copyright (C) Dyson Parra
 *
 * @history v1.0 --- La implementacion de {SeleniumScreenshot} fue realizada el 30/09/2022.
 * @dev - La primera version de {SeleniumScreenshot} fue escrita por Dyson A. Parra T.
 */
package com.project.dev.translator;

import com.google.common.io.Files;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import lombok.NonNull;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * TODO: Definición de {@code SeleniumScreenshot}.
 *
 * @author Dyson Parra
 * @since 1.8
 */
public class SeleniumScreenshot {

    /**
     * TODO: Definición de {@code mergeImages}.
     *
     * @param size
     * @param outputPath
     * @param imageName
     * @param rows
     * @param columns
     */
    public static void mergeImages(Dimension size, String outputPath, String imageName, int rows, int columns) {
        try {
            //System.out.println("Size" + size);
            BufferedImage outBuff = new BufferedImage(size.getWidth(), size.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics graphic = outBuff.getGraphics();

            int startX = 0;
            int startY = 0;
            BufferedImage first = ImageIO.read(new File(outputPath + "\\" + imageName + "-" + (1) + "-" + (1) + ".png"));
            for (int i = 0; i < rows; i++) {
                if (i != rows - 1)
                    startX = i * first.getWidth();
                else
                    startX = size.getWidth() - first.getWidth();
                for (int j = 0; j < columns; j++) {
                    File imgFile = new File(outputPath + "\\" + imageName + "-" + (i + 1) + "-" + (j + 1) + ".png");
                    BufferedImage img = ImageIO.read(imgFile);
                    imgFile.delete();
                    if (j != columns - 1)
                        startY = j * img.getHeight();
                    else
                        startY = size.getHeight() - img.getHeight();
                    graphic.drawImage(img, startX, startY, null);
                }
            }
            ImageIO.write(outBuff, "PNG", new File(outputPath + "\\" + imageName + ".png"));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * TODO: Definición de {@code getFullNodeScreenshot}.
     *
     * @param driver
     * @param node
     * @param outputPath
     * @param imgBaseName
     */
    public static void getFullNodeScreenshot(@NonNull WebDriver driver, @NonNull WebElement node, String outputPath, String imgBaseName) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            File scrFile;
            File output;
            Point location = node.getLocation();
            Dimension size = node.getSize();
            System.out.println("Pos" + location);
            System.out.println("Size" + size);

            int startX = location.getX();
            int startY = location.getY();
            int width = 0;
            int height = 0;
            int posX = 0;
            int posY = 0;
            int i = 0;
            int j = 0;

            for (i = 0; i <= 100; i++) {
                if (width * i >= size.getWidth())
                    break;
                else if ((width * (i + 1)) >= size.getWidth())
                    posX = (startX + (size.getWidth() - width));
                else
                    posX = (startX + (width * i));

                for (j = 0; j <= 100; j++) {
                    if (height * j >= size.getHeight())
                        break;
                    else if ((height * (j + 1)) >= size.getHeight())
                        posY = (startY + (size.getHeight() - height));
                    else
                        posY = (startY + (height * j));

                    js.executeScript("window.scrollTo(" + posX + "," + posY + ");");
                    //Thread.sleep(1);
                    scrFile = ((TakesScreenshot) node).getScreenshotAs(OutputType.FILE);
                    output = new File(outputPath + "\\" + imgBaseName + "-" + (i + 1) + "-" + (j + 1) + ".png");
                    Files.copy(scrFile, output);
                    if (j == 0) {
                        BufferedImage bimg = ImageIO.read(output);
                        width = bimg.getWidth();
                        height = bimg.getHeight();
                        //System.out.println("Image(" + width + ", " + height + ")");
                    }

                    //System.out.print(j + ": Start(" + posX + "," + posY + ")");
                    //System.out.println("   End(" + (posX + width) + "," + (posY + height) + ")");
                }
            }
            mergeImages(size, outputPath, imgBaseName, i, j);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

}
