package main;
import entity.SmallSatellite;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


/*HANDLES ALL ONSCREEN UI SUCH AS TEXT, ITEM ICONS ETC */
public class UI {


    final ArrayList<Message> messages = new ArrayList<>();
    private final int maxMessages = 5;
    private Color messageColour = Color.white;
    private int endOfTextY;
    public boolean starIsSelected;
    GamePanel gamePanel;
    Font arial_24B, arial_40B, arial_80B, buttonFont;
    FontMetrics fm24, fm40, fmBtn;
    BufferedImage keyImage;
    public Rectangle buildScoutButton, buildFrigateButton, buildColonyShipButton, buildBasicShipyardButton, buildSmallSatelliteButton;
    BufferedImage scoutImage, frigateImage, colonyShipImage, smallSatelliteImage, smallShipyardImage;

    private static final Color SEMI_TRANSPARENT_BLACK = new Color(0, 0, 0, 180);
    private static final Color PANEL_COLOUR = new Color(53, 53, 85);
    private static final Color BUTTON_COLOUR = new Color(46, 16, 85);
    private static final Color STATION_FRAME_COLOUR = new Color(170, 18, 18);
    private final int SCREEN_WIDTH;
    private final int SCREEN_HEIGHT;


//    public boolean messageOn = false;
    public boolean selectedMessageOn = false;
    public String selectedMessage = "";
//    public String message = "";
//    int messageCounter = 0;
    public boolean gameFinished = false;
    Star star;



    public UI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        loadImages();
        arial_24B = new Font("Arial", Font.BOLD, 24);
        buttonFont = new Font("Arial", Font.PLAIN, 20);
        arial_40B = new Font("Arial", Font.BOLD, 40);
        arial_80B = new Font("Arial", Font.BOLD, 80);



        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_WIDTH = screenSize.width;
        SCREEN_HEIGHT = screenSize.height;

       // OBJkey key = new OBJkey();
       // keyImage = key.image;
    }

    public void setFontMetrics(Graphics g) {
        g.setFont(arial_24B);
        fm24 = g.getFontMetrics();

        g.setFont(buttonFont);
        fmBtn = g.getFontMetrics();

        g.setFont(arial_40B);
        fm40 = g.getFontMetrics();
    }

    public void draw(Graphics2D graphics2D) {
        setFontMetrics(graphics2D);
        drawDate(graphics2D);
        drawMoney(graphics2D);
        if (starIsSelected && this.star != null) {
            drawStarPanel(graphics2D,star);
            drawColonisationProgress(graphics2D, star);
            drawBuildOptions(graphics2D);
        }

        for (Star star : gamePanel.starMap.getStars()) {
            if (star.colonised == Star.Colonised.BEGUN) {
                drawColonisationBarBelowStar(graphics2D, star);
            }
        }
        //TOP LEFT TEMPORARY MESSAGE
        drawMessages(graphics2D);


    }


    public void setStar(Star star) {
        this.star = star;

    }

    private void loadImages(){
        try {
            scoutImage = ImageIO.read(getClass().getResourceAsStream("/generatedImages/spaceship1.jpeg"));
            frigateImage = ImageIO.read(getClass().getResourceAsStream("/generatedImages/frigateShip.jpeg"));
            colonyShipImage = ImageIO.read(getClass().getResourceAsStream("/generatedImages/colonyShip.jpeg"));
            smallSatelliteImage = ImageIO.read(getClass().getResourceAsStream("/generatedImages/smallSatellite.jpeg"));
            smallShipyardImage = ImageIO.read(getClass().getResourceAsStream("/generatedImages/smallShipyard.jpeg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void drawDate(Graphics2D graphics2D) {
        String formattedDate = String.format("%02d / %02d / %04d",
                gamePanel.gameClock.day, gamePanel.gameClock.month, gamePanel.gameClock.year);


        int textWidth = fm40.stringWidth(formattedDate);
        int textHeight = fm40.getHeight();

        int padding = 10;

        int panelWidth = gamePanel.getWidth();
        int rectWidth = textWidth + padding * 2;
        int rectHeight = textHeight + padding * 2;
        //int x = panelWidth - rectWidth - 20; // 20 px from the right edge
        int x = 20; // 20 from left
        int y = gamePanel.getHeight() - rectHeight - 20; // 20 px from the bottom

        graphics2D.setColor(SEMI_TRANSPARENT_BLACK); // semi-transparent black
        graphics2D.fillRoundRect(x, y, rectWidth, rectHeight, 10, 10);
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawRoundRect(x, y, rectWidth, rectHeight, 10, 10);
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString(formattedDate, x + padding, y + padding + fm40.getAscent());
    }

  private void drawStarPanel(Graphics2D graphics2D, Star star) {
      graphics2D.setFont(arial_40B);

      int panelWidth = SCREEN_WIDTH / 5;
      graphics2D.setColor(PANEL_COLOUR);
      graphics2D.fillRect(SCREEN_WIDTH - panelWidth, 0, panelWidth, SCREEN_HEIGHT);

      graphics2D.setColor(Color.WHITE);

      String message = star.name;
      int textWidth = fm40.stringWidth(message);
      int textHeight = fm40.getHeight();
      int panelStartX = SCREEN_WIDTH - panelWidth;
      int x = panelStartX + (panelWidth - textWidth) / 2;
      graphics2D.drawString(message, x,65);


      graphics2D.setFont(arial_24B);


      if (!gamePanel.visitedStars.contains(star)) {
          message = "Quality: UNKNOWN";
      } else {
          message = "Quality: " + star.quality;
      }
      textWidth = fm24.stringWidth(message);
      panelStartX = SCREEN_WIDTH - panelWidth;
      x = panelStartX + (panelWidth - textWidth) / 2;
      textHeight+=70;
      graphics2D.drawString(message, x,textHeight);


      if (!gamePanel.visitedStars.contains(star)) {
          message = "Status: UNEXPLORED";
      } else {
          message = "Status: " + star.colonised;
      }
      textWidth = fm24.stringWidth(message);
      panelStartX = SCREEN_WIDTH - panelWidth;
      x = panelStartX + (panelWidth - textWidth) / 2;
      textHeight+=50;
      graphics2D.drawString(message, x,textHeight);

      message = "Population:";
      textWidth = fm24.stringWidth(message);
      panelStartX = SCREEN_WIDTH - panelWidth;
      x = panelStartX + (panelWidth - textWidth) / 2;
      textHeight+=50;
      graphics2D.drawString(message, x,textHeight);

      //unvisited
      if (!gamePanel.visitedStars.contains(star)) {
          message = "UNKNOWN";
          //colonised
      } else if (star.colonised == Star.Colonised.COLONISED) {
          message = String.format("%,d", star.population);
      } else {
          message = "None";
      }
      graphics2D.setFont(buttonFont);
      textWidth = fmBtn.stringWidth(message);
      panelStartX = SCREEN_WIDTH - panelWidth;
      x = panelStartX + (panelWidth - textWidth) / 2;
      textHeight+=30;
      graphics2D.drawString(message, x,textHeight);


      if (star.station != null) {
          message = "Station: " + star.station.name;
      } else {
          message = "Station: None";
      }
      graphics2D.setFont(arial_24B);
      textWidth = fm24.stringWidth(message);
      panelStartX = SCREEN_WIDTH - panelWidth;
      x = panelStartX + (panelWidth - textWidth) / 2;
      textHeight+=50;
      graphics2D.drawString(message, x,textHeight);

      message = "Satellites:";
      textWidth = fm24.stringWidth(message);
      panelStartX = SCREEN_WIDTH - panelWidth;
      x = panelStartX + (panelWidth - textWidth) / 2;
      textHeight+=50;
      graphics2D.drawString(message, x,textHeight);

      if (star.satellites.isEmpty()) {
          message = "No Satellites";
      } else {
          message = star.satellites.getFirst().name + " x " + star.satellites.size();
      }
      graphics2D.setFont(buttonFont);
      textWidth = fmBtn.stringWidth(message);
      panelStartX = SCREEN_WIDTH - panelWidth;
      x = panelStartX + (panelWidth - textWidth) / 2;
      textHeight+=30;
      graphics2D.drawString(message, x,textHeight);




      int padding = 10;
      int frameWidth = 40;
      int buttonWidth = 240;
      int totalContentWidth = padding + frameWidth + buttonWidth + padding;
      int layoutStartX = panelStartX + (panelWidth - totalContentWidth) / 2;

      graphics2D.setFont(buttonFont);
      endOfTextY = textHeight;

  }
    private void drawBuildOptions(Graphics2D graphics2D) {
        int padding = 10;
        int frameWidth = 40;
        int buttonWidth = 240;
        int panelWidth = SCREEN_WIDTH / 5;
        int panelStartX = SCREEN_WIDTH - panelWidth;
        int totalContentWidth = padding + frameWidth + buttonWidth + padding;
        int layoutStartX = panelStartX + (panelWidth - totalContentWidth) / 2;

        graphics2D.setFont(buttonFont);
        int y = endOfTextY + 20;
        int resetHeight = y;

        if (star.station == null && star.colonised == Star.Colonised.COLONISED) {

            Rectangle stationFrame1 = new Rectangle(layoutStartX + padding, y, frameWidth, 40);


            Rectangle buildBasicShipyardButton = new Rectangle(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            graphics2D.setColor(BUTTON_COLOUR);
            graphics2D.fill(buildBasicShipyardButton);

            graphics2D.setColor(STATION_FRAME_COLOUR);
            graphics2D.fill(stationFrame1);


            graphics2D.setColor(Color.WHITE);
            String text = "Build Basic Shipyard";
            int textWidth = fmBtn.stringWidth(text);
            int textX = buildBasicShipyardButton.x + (buildBasicShipyardButton.width - textWidth) / 2;
            int textY = buildBasicShipyardButton.y + (buildBasicShipyardButton.height + fmBtn.getAscent()) / 2 - 2;
            graphics2D.drawString(text, textX, textY);


            graphics2D.drawImage(smallShipyardImage, layoutStartX + padding, y, frameWidth, 40, null);

            this.buildBasicShipyardButton = buildBasicShipyardButton;


        } else if (star.station!= null && star.station.name.equals("Basic Shipyard")) {
            Rectangle buildScoutButton = new Rectangle(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            Rectangle unitFrame1 = new Rectangle(layoutStartX + padding, y, frameWidth, 40);
            y = y + 50;
            Rectangle buildFrigateButton = new Rectangle(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            Rectangle unitFrame2 = new Rectangle(layoutStartX + padding, y, frameWidth, 40);

            y = y + 50;
            Rectangle buildColonyShipButton = new Rectangle(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            Rectangle unitFrame3 = new Rectangle(layoutStartX + padding, y, frameWidth, 40);


            graphics2D.setColor(Color.LIGHT_GRAY);
            graphics2D.fill(unitFrame1);
            graphics2D.fill(unitFrame2);
            graphics2D.fill(unitFrame3);

            // BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/units/ScoutShipRight.png"));
            //graphics2D.drawImage(image, x - 45, 270, gamePanel.TILE_SIZE *3, gamePanel.TILE_SIZE * 3, null);
            y = resetHeight;

//                    BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/generatedImages/spaceship1.jpeg"));

            graphics2D.drawImage(scoutImage, layoutStartX + padding, y, frameWidth, 40, null);
            y+=50;


//                    image = ImageIO.read(getClass().getResourceAsStream("/generatedImages/frigateShip.jpeg"));
            graphics2D.drawImage(frigateImage, layoutStartX + padding, y, frameWidth, 40, null);

            y+=50;

//                    image = ImageIO.read(getClass().getResourceAsStream("/generatedImages/colonyShip.jpeg"));
            graphics2D.drawImage(colonyShipImage, layoutStartX + padding, y, frameWidth, 40, null);


            // Draw the buttons
            graphics2D.setColor(BUTTON_COLOUR);
            graphics2D.fill(buildScoutButton);
            graphics2D.fill(buildFrigateButton);
            graphics2D.fill(buildColonyShipButton);

            graphics2D.setColor(Color.WHITE);

            String name = "Scout";
            String text =  name+"   ₡" + gamePanel.buildCosts.get(name) ;
            int textWidth = fmBtn.stringWidth(text);
            int textX = buildScoutButton.x + (buildScoutButton.width - textWidth) / 2;
            int textY = buildScoutButton.y + (buildScoutButton.height + fmBtn.getAscent()) / 2 - 2;
            graphics2D.drawString(text, textX, textY);
            name = "Frigate";
            text =  name+"   ₡" + gamePanel.buildCosts.get(name) ;
            textWidth = fmBtn.stringWidth(text);
            textX = buildFrigateButton.x + (buildFrigateButton.width - textWidth) / 2;
            textY = buildFrigateButton.y + (buildFrigateButton.height + fmBtn.getAscent()) / 2 - 2;
            graphics2D.drawString(text, textX, textY);
            name = "Colony Ship";
            text =  name+"   ₡" + gamePanel.buildCosts.get(name) ;
            textWidth = fmBtn.stringWidth(text);
            textX = buildColonyShipButton.x + (buildColonyShipButton.width - textWidth) / 2;
            textY = buildColonyShipButton.y + (buildColonyShipButton.height + fmBtn.getAscent()) / 2 - 2;
            graphics2D.drawString(text, textX, textY);
            // Store for later
            this.buildScoutButton = buildScoutButton;
            this.buildFrigateButton = buildFrigateButton;
            this.buildColonyShipButton = buildColonyShipButton;






        }

        if (star.colonised == Star.Colonised.COLONISED) {
            y = y + 50;
            Rectangle buildSmallSatelliteButton = new Rectangle(layoutStartX + padding + frameWidth, y, buttonWidth, 40);
            Rectangle satelliteFrame1 = new Rectangle(layoutStartX + padding, y, frameWidth, 40);

            graphics2D.setColor(Color.LIGHT_GRAY);
            graphics2D.fill(satelliteFrame1);
            graphics2D.setColor(BUTTON_COLOUR);
            graphics2D.fill(buildSmallSatelliteButton);
            //                    BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/generatedImages/smallSatellite.jpeg"));

            graphics2D.drawImage(smallSatelliteImage, layoutStartX + padding, y, frameWidth, 40, null);

            String name = "Small Satellite";
            String text =  name+"   ₡" + gamePanel.buildCosts.get(name) ;
            graphics2D.setFont(buttonFont);
            graphics2D.setColor(Color.white);
            int textWidth = fmBtn.stringWidth(text);
            int textX = buildSmallSatelliteButton.x + (buildSmallSatelliteButton.width - textWidth) / 2;
            int textY = buildSmallSatelliteButton.y + (buildSmallSatelliteButton.height + fmBtn.getAscent()) / 2 - 2;
            graphics2D.drawString(text, textX, textY);

            this.buildSmallSatelliteButton = buildSmallSatelliteButton;


            LinkedList<BuildTask> queue = gamePanel.shipBuilderHelper.starQueues.get(star);
            if (queue != null && !queue.isEmpty()) {
                BuildTask buildTask = queue.peek();
                        int x = layoutStartX + padding;
                        y = resetHeight;
                        switch (buildTask.buildType) {
                            case "scout":
                                drawProgressBar(graphics2D, x, y, 40, 40, buildTask.getProgress());
                                break;
                            case "frigate":

                                drawProgressBar(graphics2D, x, y + 50, 40, 40, buildTask.getProgress());
                                break;
                            case "colonyship":
                                drawProgressBar(graphics2D, x, y + 100, 40, 40, buildTask.getProgress());
                                break;

                            case "basicshipyard":
                                drawProgressBar(graphics2D, x, y, 40, 40, buildTask.getProgress());
                                break;


                        }
                        if (buildTask.buildType.equals("smallsatellite")) {
                            if (star.station != null) {
                                drawProgressBar(graphics2D, x, y + 150, 40, 40, buildTask.getProgress());
                            } else {
                                drawProgressBar(graphics2D, x, y + 50, 40, 40, buildTask.getProgress());
                            }
                        }

//                    }

            }
        }
    }
    private void drawColonisationProgress(Graphics2D graphics2D, Star star) {
        if (star.colonised != Star.Colonised.BEGUN) {
            return;
        }
        graphics2D.setFont(arial_24B);

        graphics2D.setColor(Color.WHITE);

        String message = "Colonisation Progress:";

        int textWidth = fm24.stringWidth(message);
        int panelWidth = SCREEN_WIDTH / 5;
        int textHeight = fm24.getHeight();
        int panelStartX = SCREEN_WIDTH - panelWidth;
        int x = panelStartX + (panelWidth - textWidth) / 2;
        graphics2D.drawString(message, x,endOfTextY + 50);

        double progress = (gamePanel.gameClock.getTotalGameDays() - star.colonisationStartDate) / 180.0;
        int barX = panelStartX + 20;
        int barY = endOfTextY + 90;
        int barWidth = panelWidth - 40;
        int barHeight = 40;
        drawProgressBar(graphics2D, barX, barY, barWidth, barHeight, progress);


    }

    private void drawColonisationBarBelowStar(Graphics2D graphics2D, Star star) {
        double progress = (gamePanel.gameClock.getTotalGameDays() - star.colonisationStartDate) / 180.0;
        int barWidth = 20;
        int barX = (int) (star.x - ((float) barWidth / 2));
       int barY = (int) star.y + 15;
        int barHeight = 10;
        drawProgressBar(graphics2D, barX, barY, barWidth, barHeight, progress);
    }



    public void addMessage(String text) {
        if (messages.size() >= maxMessages) {
            messages.remove(0); // remove oldest
        }
        messageColour= Color.white;
        messages.add(new Message(text, 3.0)); // 3 in game days
    }

    public void addMessage(String text, String colour) {
        if (messages.size() >= maxMessages) {
            messages.remove(0); // remove oldest
        }

        switch (colour) {
            case "green":
                messageColour = new Color(5, 211, 18);
                break;
            case "red":
                messageColour = new Color(255, 0, 0);
                break;
            default:
                messageColour = Color.white;

        }
        messages.add(new Message(text, 3.0)); // adjust lifetime if needed
    }

    private void drawMoney(Graphics2D g2) {
        String moneyText = "₡" + gamePanel.money;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        FontMetrics fm = g2.getFontMetrics();

        int textWidth = fm.stringWidth(moneyText);
        int x = (SCREEN_WIDTH / 2) - (textWidth / 2);
        int y = gamePanel.TILE_SIZE;

        g2.setColor(new Color(0, 0, 0, 150)); // Optional background
        g2.fillRoundRect(x - 8, y - 24, textWidth + 16, 28, 10, 10);

        g2.setColor(Color.YELLOW);
        g2.drawString(moneyText, x, y);
    }

    private void drawMessages(Graphics2D g2) {
        int x = 20;
        int y = 20;
        int lineHeight = 20;

        g2.setFont(new Font("Arial", Font.PLAIN, 14));

        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            g2.drawString(msg.text, x, y + i * lineHeight);
        }

        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            int drawY = y + i * lineHeight;

            // Background box
            g2.setColor(SEMI_TRANSPARENT_BLACK); // translucent black
            g2.fillRoundRect(x - 5, drawY - 15, g2.getFontMetrics().stringWidth(msg.text) + 10, 20, 10, 10);

            g2.setColor(messageColour);

            g2.drawString(msg.text, x, drawY);
        }

    }

    public void updateMessages(double elapsedDays) {
        Iterator<Message> iterator = messages.iterator();
        while (iterator.hasNext()) {
            Message msg = iterator.next();
            msg.life -= elapsedDays;
            if (msg.life <= 0) {
                iterator.remove();
            }
        }
    }

    public void drawProgressBar(Graphics2D g2, int x, int y, int width, int height, double progress) {
        // Clamp progress between 0.0 and 1.0
        progress = Math.max(0.0, Math.min(1.0, progress));

        // Draw border
        g2.setColor(Color.white);
        g2.drawRect(x, y, width, height);

        // Fill progress
        int filledWidth = (int) (width * progress);
        g2.setColor(Color.green);
        g2.fillRect(x + 1, y + 1, filledWidth - 1, height - 1);
    }


}
