import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;


import javax.swing.JPanel;

public class gamePanel extends JPanel implements Runnable{
    
    //SCREEN SETTINGS
    final static int originalTileSize = 8; //16x16 tile
    final static int scale = 3;

    final static int tileSize = originalTileSize * scale; //48x48 tile
    final static int maxScreenCol = 60;// 1440 x pixels
    final static int maxScreenRow = 60;// 1440 y pixels
    final static int screenWidth = tileSize * maxScreenCol;
    final static int screenHeight = tileSize * maxScreenRow;
    final static vector gravity = new vector(.1f, 0f);
    final static float density = 1f;
    final static float o = 1.9f; // should be between 1 and 2, higher values mean more diffusion and is the overrelaxation factor
    
    public static float[][] arrayU = new float[maxScreenRow][maxScreenCol];
    public static float[][] arrayV = new float[maxScreenRow][maxScreenCol];
    public static float[][] arrayS = new float[maxScreenRow][maxScreenCol];
    public static float[][] arrayP = new float[maxScreenRow][maxScreenCol];
    public static float[][] newArrayU = new float[maxScreenRow][maxScreenCol];
    public static float[][] newArrayV = new float[maxScreenRow][maxScreenCol];

    //FPS
    int FPS = 60;

    ArrayList<object> Objects = new ArrayList<object>();

    Thread gameThread;

    public gamePanel() {
        
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
    }

    public void startGameThread(){

        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run(){

        for(int i = 0; i < maxScreenCol; i++){
            for(int j  = 0; j < maxScreenRow; j++){
                if(i < maxScreenCol && i > 0 && j < maxScreenRow - 1 && j > 0){
                    arrayU[j][i] = 1f;
                    arrayV[j][i] = 1f;
                    arrayS[j][i] = 1f;
                    arrayP[j][i] = 0;
                }
                else{
                    arrayU[j][i] = 0f;
                    arrayV[j][i] = 0f;
                    arrayS[j][i] = 0f;
                    arrayP[j][i] = 0;
                }
            }
        }
        /**for(int i = 0; i < maxScreenCol; i++){
            for(int j  = 0; j < maxScreenRow; j++){
                if(i == 0 || j == 0 || i == maxScreenCol - 1 || j == maxScreenRow - 1){
                    object newObject = new object(new vector(i * tileSize, j * tileSize), 5, new vector(0, 0), true);
                    Objects.add(newObject);

                }
                else if(i <= 10 && j <= 10){
                    object newObject = new object(new vector(i * tileSize, j * tileSize), 5, new vector(1, 1), false);
                    Objects.add(newObject);
                }
                else{
                    object newObject = new object(new vector(i * tileSize, j * tileSize), 5, new vector(0, 0), false);
                    Objects.add(newObject);
                }
            }
        }**/

        

        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null){

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;

            lastTime = currentTime;

            if(delta >= 1){
                update(delta);
                repaint();
                delta--;
            }

        }
    }

        // double drawInterval = 1000000000/FPS; //0.01666 seconds
        // double nextDrawTime = System.nanoTime() + drawInterval;

        // while(gameThread != null){

        //     // 1 UPDATE: update information such as character positions
        //     update(drawInterval);

        //     //2 DRAW: draw the screen with update information.
        //     repaint();

        //     try {
        //         double remainingTime = nextDrawTime - System.nanoTime();
        //         remainingTime = remainingTime/1000000;

        //         if(remainingTime < 0){
        //             remainingTime = 0;
        //         }

        //         Thread.sleep((long) remainingTime);

        //         nextDrawTime += drawInterval;

        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        // }
    //}



    public void update(double drawInt){
        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                // object obj = Objects.get(i * j);
                // if(obj.boundary){
                //     continue;
                // }

                //obj.updateGravity(gravity, drawInt);
                
                // object leftobj = Objects.get(i * (j - 1));
                // object rightobj = Objects.get(i * (j + 1));
                // object upobj = Objects.get((i-1) * j);
                // object downobj = Objects.get((i+1) * j);
                // obj.updateDiffusion(0.5f, leftobj, rightobj, upobj, downobj);
                // System.out.println(obj.velocity.x + ", " + obj.velocity.y);

                vector divergence = new vector(0f, 0f);
                divergence.x = arrayU[i + 1][j] - arrayU[i][j];
                divergence.y = arrayV[i][j + 1] - arrayV[i][j];
                divergence = divergence.multiply(o);

                float s = arrayS[i + 1][j] + arrayS[i - 1][j] + arrayS[i][j + 1] + arrayS[i][j - 1];

                arrayP[i][j] = (float)(arrayP[i][j] + (divergence.magnitude()/s) * (density * tileSize / drawInt));

                float x = (float)(i * tileSize * Math.random());
                float y = (float)(j * tileSize * Math.random());

                float w00 = 1 - x/tileSize;
                float w01 = x/tileSize;
                float w10 = 1 - y/tileSize;
                float w11 = y/tileSize;

                float vbar = (w00 * w10 * arrayV[i][j]) + (w01 * w10 * arrayV[i + 1][j]) + (w00 * w11 * arrayV[i][j + 1]) + (w01 * w11 * arrayV[i + 1][j + 1]);

                vector vel = new vector(arrayU[i][j], vbar);

                if(arrayU[i][j] == 0f){
                    arrayU[i + 1][j] = arrayU[i + 1][j] - divergence.x * arrayS[i+1][j]/s;
                    arrayV[i][j] = arrayV[i][j] + divergence.y * arrayS[i][j - 1]/s;
                    arrayV[i][j + 1] = arrayV[i][j + 1] - divergence.y * arrayS[i][j+1]/s;

                }
                else if(arrayU[i + 1][j] == 0f){
                    arrayU[i][j] = arrayU[i][j] + divergence.x * arrayS[i-1][j]/s;
                    arrayV[i][j] = arrayV[i][j] + divergence.y * arrayS[i][j - 1]/s;
                    arrayV[i][j + 1] = arrayV[i][j + 1] - divergence.y * arrayS[i][j+1]/s;
                }
                else if(arrayV[i][j] == 0f){
                    arrayU[i][j] = arrayU[i][j] + divergence.x * arrayS[i-1][j]/s;
                    arrayU[i + 1][j] = arrayU[i + 1][j] - divergence.x * arrayS[i+1][j]/s;
                    arrayV[i][j + 1] = arrayV[i][j + 1] - divergence.y * arrayS[i][j+1]/s;
                }
                else if(arrayV[i][j + 1] == 0f){
                    arrayU[i][j] = arrayU[i][j] + divergence.x * arrayS[i-1][j]/s;
                    arrayU[i + 1][j] = arrayU[i + 1][j] - divergence.x * arrayS[i+1][j]/s;
                    arrayV[i][j] = arrayV[i][j] + divergence.y * arrayS[i][j - 1]/s;
                }
                else{
                    arrayU[i][j] = arrayU[i][j] + divergence.x * arrayS[i-1][j]/s;
                    arrayU[i + 1][j] = arrayU[i + 1][j] - divergence.x * arrayS[i+1][j]/s;
                    arrayV[i][j] = arrayV[i][j] + divergence.y * arrayS[i][j - 1]/s;
                    arrayV[i][j + 1] = arrayV[i][j + 1] - divergence.y * arrayS[i][j+1]/s;
                }

                // float newX = x / tileSize + vel.x * (float)drawInt / tileSize;
                // float newY = y / tileSize + vel.y * (float)drawInt / tileSize;

                // int targetI = Math.max(0, Math.min(maxScreenRow - 1, (int)(i + newX)));
                // int targetJ = Math.max(0, Math.min(maxScreenCol - 1, (int)(j + newY)));

                // arrayU[targetI][targetJ] = arrayU[i][j];
                // arrayV[targetI][targetJ] = arrayV[i][j];

                //FIX ADVECTION BETWEEN CELLS

                
                //PRESSURE NOT NEEDED BUT COULD BE USED FOR VISUALIZATION OR IMPROVEMENTS LATER

                // vector divergence = rightVect.velocity.subVector(leftVect.velocity).addVector(upVect.velocity.subVector(downVect.velocity));
        
                // if(leftVect.boundary == true || downVect.boundary == true){
                //     this.velocity = this.velocity.subVector(divergence.divide(3f));
                // }
                // else if(rightVect.boundary == true || upVect.boundary == true){
                //     this.velocity = this.velocity.addVector(divergence.divide(3f));
                // }
                // else{
                //     this.velocity = this.velocity.addVector(divergence.divide(4f));
                // }

            }
        }

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                newArrayU[i][j] = arrayU[i][j];
                newArrayV[i][j] = arrayV[i][j];
                System.out.println(newArrayU[i][j] + ", " + newArrayV[i][j]);
            }
        }

    }
    
@Override
    public void paintComponent(Graphics g){

        super.paintComponent(g);

        Graphics2D graphics = (Graphics2D)g;

        // for(int i = 0; i < maxScreenRow; i++){
        //     for(int j  = 0; j < maxScreenCol; j++){
        //         graphics.drawRect(i * tileSize, j * tileSize, tileSize, tileSize);
        //     }
        // }

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
            float colorValue = Math.max(0.0f, Math.min(1.0f, (arrayP[i][j])/3f));
            int rgb = (int)(colorValue * 255);
            graphics.setColor(new Color(rgb, 1, 1));

                graphics.fillRect(i * tileSize, j * tileSize, tileSize, tileSize);
                //graphics.drawLine((int)(obj.position.x), (int)(obj.position.y + 0.5 * tileSize), (int)(obj.position.x + obj.velocity.x), (int)(obj.position.y + 0.5 * tileSize));
                //graphics.drawLine((int)(obj.position.x + 0.5 * tileSize), (int)(obj.position.y), (int)(obj.position.x  + 0.5 * tileSize), (int)(obj.position.y + obj.velocity.y));
            }
        }
        // for(object obj: Objects){
        //     graphics.setColor(Color.white);
        //     graphics.drawLine((int)(obj.position.x), (int)(obj.position.y + 0.5 * tileSize), (int)(obj.position.x + obj.velocity.x), (int)(obj.position.y + 0.5 * tileSize));
        //     graphics.drawLine((int)(obj.position.x + 0.5 * tileSize), (int)(obj.position.y), (int)(obj.position.x  + 0.5 * tileSize), (int)(obj.position.y + obj.velocity.y));
        // }

        graphics.setColor(Color.white);

        //graphics.setColor(Color.CYAN);
        //main_Branch.drawBranch(graphics);
    }

}
