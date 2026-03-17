import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import java.util.Random;

public class gamePanel extends JPanel implements Runnable{
    
    //SCREEN SETTINGS
    final static int originalTileSize = 4; //16x16 tile
    final static int scale = 4;

    final static int tileSize = originalTileSize * scale; //48x48 tile
    final static int maxScreenCol = 60;// 1440 x pixels
    final static int maxScreenRow = 60;// 1440 y pixels
    final static int screenWidth = tileSize * maxScreenCol;
    final static int screenHeight = tileSize * maxScreenRow;
    final static vector gravity = new vector(0f, 0f);
    final static float density = 1f;
    final static float o = 1.1f; // should be between 1 and 2, higher values mean more diffusion and is the overrelaxation factor
    
    public static float[][] arrayU = new float[maxScreenRow][maxScreenCol];
    public static float[][] arrayV = new float[maxScreenRow][maxScreenCol];
    public static float[][] arrayS = new float[maxScreenRow][maxScreenCol];
    public static float[][] arrayP = new float[maxScreenRow][maxScreenCol];
    public static float[][] arrayR = new float[maxScreenRow][maxScreenCol];
    public static float[][] newArrayU = new float[maxScreenRow][maxScreenCol];
    public static float[][] newArrayV = new float[maxScreenRow][maxScreenCol];
    public static float[][] newArrayS = new float[maxScreenRow][maxScreenCol];
    public static float[][] newArrayR = new float[maxScreenRow][maxScreenCol];

    Random random = new Random();

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

        for(int i = 0; i < maxScreenRow; i++){
            for(int j  = 0; j < maxScreenCol; j++){
                if (i == 0 || i == maxScreenRow - 1 || j == 0 || j == maxScreenCol - 1) {
                    arrayP[i][j] = 1; // solid border
                    arrayU[i][j] = 0f;
                    arrayV[i][j] = 0f;
                    arrayS[i][j] = 0f;
                    arrayR[i][j] = 0f;
                } else {
                    arrayP[i][j] = 0; // fluid interior
                    arrayU[i][j] = 0f;
                    arrayV[i][j] = 0f;
                    arrayS[i][j] = 0f;
                    arrayR[i][j] = 0f;
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

        double delta_time;

        while(gameThread != null){

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;

            delta_time = 1.0/60.0;

            lastTime = currentTime;

            if(delta >= 1){
                update(delta_time);
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

    public void updateGravity(double drawInt){
        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){

                if(arrayP[i][j] == 1){
                    continue;
                }

                //gravity.x = random.nextFloat(100) - 50f; // random horizontal gravity 
                //gravity.y = random.nextFloat() * 10f - 5f; // random vertical gravity between -5 and 5

                vector grav = new vector(gravity.x * (float)drawInt ,gravity.y * (float)drawInt); //velocity at center of cell, no interpolation for now

                arrayU[i][j] += grav.x;
                arrayV[i][j] += grav.y;

                newArrayU[i][j] = arrayU[i][j];
                newArrayV[i][j] = arrayV[i][j];
            }
        }

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                arrayU[i][j] = newArrayU[i][j];
                arrayV[i][j] = newArrayV[i][j];
            }
        }
    }

    public void addBuoyancy(double drawInt){
        float safeDt = (float)Math.max(drawInt, 1e-6);
        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                //For smoke simulation, we can add a buoyancy force based on the density of the fluid. This will cause less dense fluid (like smoke) to rise and denser fluid to sink. The buoyancy force can be calculated as follows:
                float buoyancyStrength = 0.1f; // Adjust this value to control the
                arrayV[i][j] -= buoyancyStrength * arrayS[i][j] * safeDt;
            }
        }
    }

    public void clearPressure(){
        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                arrayR[i][j] = 0f;
            }
        }
    }

    public void solvePressure(double drawInt){
        float safeDt = (float)Math.max(drawInt, 1e-6);

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                arrayU[i][j] = newArrayU[i][j];
                arrayV[i][j] = newArrayV[i][j];
            }
        }

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){

                if(arrayP[i][j] == 1){
                    continue;
                }

                boolean leftSolid = arrayP[i][j - 1] == 1;
                boolean rightSolid = arrayP[i][j + 1] == 1;
                boolean upSolid = arrayP[i - 1][j] == 1;
                boolean downSolid = arrayP[i + 1][j] == 1;

                int openNeighbors = 0;
                if(!leftSolid) openNeighbors++;
                if(!rightSolid) openNeighbors++;
                if(!upSolid) openNeighbors++;
                if(!downSolid) openNeighbors++;

                if(openNeighbors == 0){
                    continue;
                }

                float leftPressure = leftSolid ? 0f : arrayR[i][j - 1];
                float rightPressure = rightSolid ? 0f : arrayR[i][j + 1];
                float upPressure = upSolid ? 0f : arrayR[i - 1][j];
                float downPressure = downSolid ? 0f : arrayR[i + 1][j];

                float leftVel = leftSolid ? 0f : arrayU[i][j - 1];
                float rightVel = rightSolid ? 0f : arrayU[i][j + 1];
                float upVel = upSolid ? 0f : arrayV[i - 1][j];
                float downVel = downSolid ? 0f : arrayV[i + 1][j];

                // U[i][j+1] is the right face, U[i][j] is the left face — both are true face velocities
                float divergence = (arrayU[i][j + 1] - arrayU[i][j]) +
                                (arrayV[i + 1][j] - arrayV[i][j]);


                newArrayR[i][j] = ((leftPressure + rightPressure + upPressure + downPressure - divergence) / openNeighbors);


            }
        }

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                arrayR[i][j] = newArrayR[i][j];
            }
        }
    }

    public void updatePressure(double drawInt){
        float safeDt = (float)Math.max(drawInt, 1e-6);
        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){

                if(arrayP[i][j] == 1){
                    continue;
                }

                boolean leftSolid = arrayP[i][j - 1] == 1;
                boolean rightSolid = arrayP[i][j + 1] == 1;
                boolean upSolid = arrayP[i - 1][j] == 1;
                boolean downSolid = arrayP[i + 1][j] == 1;

                float leftPressure = leftSolid ? arrayR[i][j] : arrayR[i][j - 1];
                float rightPressure = rightSolid ? arrayR[i][j] : arrayR[i][j + 1];
                float upPressure = upSolid ? arrayR[i][j] : arrayR[i - 1][j];
                float downPressure = downSolid ? arrayR[i][j] : arrayR[i + 1][j];


                // Correct: project onto faces
                newArrayU[i][j]     = arrayU[i][j]     - (arrayR[i][j] - leftPressure);
                newArrayU[i][j + 1] = arrayU[i][j + 1] - (rightPressure - arrayR[i][j]);
                newArrayV[i][j]     = arrayV[i][j]     - (arrayR[i][j] - upPressure);
                newArrayV[i + 1][j] = arrayV[i + 1][j] - (downPressure - arrayR[i][j]);

            }
        }

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                arrayU[i][j] = newArrayU[i][j];
                arrayV[i][j] = newArrayV[i][j];
            }
        }
    }

    public void advectVelocity(double drawInt){
        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){

                if(arrayP[i][j] == 1){
                    continue;
                }

                //INTERPOLATING THE VELOCITY AT THE CENTER OF THE CELL

                // For U component — backtrace from left face of cell
                float Ux = (float) j;        // face is at left edge, not center
                float Uy = (float) i + 0.5f;

                // For V component — backtrace from top face of cell  
                float Vx = (float) j + 0.5f;
                float Vy = (float) i;        // face is at top edge, not center

                float newUX = Ux - (float)drawInt * arrayU[i][j];
                float newUY = Uy - (float)drawInt * arrayV[i][j];

                newUX = (float)Math.max(1, (float)Math.min(maxScreenCol - 2, newUX));
                newUY = (float)Math.max(1, (float)Math.min(maxScreenRow - 2, newUY));

                //INTERPOLATING THE BACKTRACED POSITION TO FIND THE NEW VELOCITY!

                // Extract integer part for grid indices
                int Ui0 = (int)Math.floor(newUY);
                int Ui1 = Ui0 + 1;
                float fracUY = newUY - Ui0; //fractional offset

                int Uj0 = (int)Math.floor(newUX);
                int Uj1 = Uj0 + 1;
                float fracUX = newUX - Uj0;

                // Clamp indices to be within bounds
                Ui0 = Math.max(1, Math.min(maxScreenRow - 2, Ui0));
                Ui1 = Math.max(1, Math.min(maxScreenRow - 2, Ui1));
                Uj0 = Math.max(1, Math.min(maxScreenCol - 2, Uj0));
                Uj1 = Math.max(1, Math.min(maxScreenCol - 2, Uj1));

                //Compute Weights
                float newwU00 = (1 - fracUY) * (1 - fracUX);
                float newwU10 = fracUY * (1 - fracUX);
                float newwU01 = (1 - fracUY) * fracUX;
                float newwU11 = fracUY * fracUX;

                // Interpolate the **value** (stays as float)
                float interpolatedU = newwU00 * arrayU[Ui0][Uj0] + newwU10 * arrayU[Ui1][Uj0] + 
                                    newwU01 * arrayU[Ui0][Uj1] + newwU11 * arrayU[Ui1][Uj1];

                float newVX = Vx - (float)drawInt * arrayU[i][j];
                float newVY = Vy - (float)drawInt * arrayV[i][j];

                newVX = (float)Math.max(1, (float)Math.min(maxScreenCol - 2, newVX));
                newVY = (float)Math.max(1, (float)Math.min(maxScreenRow - 2, newVY));

                //INTERPOLATING THE BACKTRACED POSITION TO FIND THE NEW VELOCITY!

                // Extract integer part for grid indices
                int Vi0 = (int)Math.floor(newVY);
                int Vi1 = Vi0 + 1;
                float fracVY = newVY - Vi0; //fractional offset

                int Vj0 = (int)Math.floor(newVX);
                int Vj1 = Vj0 + 1;
                float fracVX = newVX - Vj0;

                // Clamp indices to be within bounds
                Vi0 = Math.max(1, Math.min(maxScreenRow - 2, Vi0));
                Vi1 = Math.max(1, Math.min(maxScreenRow - 2, Vi1));
                Vj0 = Math.max(1, Math.min(maxScreenCol - 2, Vj0));
                Vj1 = Math.max(1, Math.min(maxScreenCol - 2, Vj1));

                //Compute Weights
                float newwV00 = (1 - fracVY) * (1 - fracVX);
                float newwV10 = fracVY * (1 - fracVX);
                float newwV01 = (1 - fracVY) * fracVX;
                float newwV11 = fracVY * fracVX;


                float interpolatedV = newwV00 * arrayV[Vi0][Vj0] + newwV10 * arrayV[Vi1][Vj0] + 
                                    newwV01 * arrayV[Vi0][Vj1] + newwV11 * arrayV[Vi1][Vj1];

                //int targetI = (int)Math.max(0, Math.min(maxScreenRow - 1, (newX)));
                //int targetJ = (int)Math.max(0, Math.min(maxScreenCol - 1, (newY)));

                newArrayU[i][j] = interpolatedU;
                newArrayV[i][j] = interpolatedV;

            }
        }

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                arrayU[i][j] = newArrayU[i][j];
                arrayV[i][j] = newArrayV[i][j];

            }
        }
    }

    public void advectDensity(double drawInt){
        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){

                if(arrayP[i][j] == 1){
                    continue;
                }

                //INTERPOLATING THE VELOCITY AT THE CENTER OF THE CELL
 
                float x = (float)(j) + 0.5f;
                float y = (float)(i) + 0.5f;

                float newX = x - (float)drawInt * arrayU[i][j];
                float newY = y - (float)drawInt * arrayV[i][j];

                newX = (float)Math.max(1, (float)Math.min(maxScreenCol - 2, newX));
                newY = (float)Math.max(1, (float)Math.min(maxScreenRow - 2, newY));

                //INTERPOLATING THE BACKTRACED POSITION TO FIND THE NEW VELOCITY!

                // Extract integer part for grid indices
                int i0 = (int)Math.floor(newY);
                int i1 = i0 + 1;
                float fracY = newY - i0; //fractional offset

                int j0 = (int)Math.floor(newX);
                int j1 = j0 + 1;
                float fracX = newX - j0;

                // Clamp indices to be within bounds
                i0 = Math.max(1, Math.min(maxScreenRow - 2, i0));
                i1 = Math.max(1, Math.min(maxScreenRow - 2, i1));
                j0 = Math.max(1, Math.min(maxScreenCol - 2, j0));
                j1 = Math.max(1, Math.min(maxScreenCol - 2, j1));

                //Compute Weights
                float neww00 = (1 - fracY) * (1 - fracX);
                float neww10 = fracY * (1 - fracX);
                float neww01 = (1 - fracY) * fracX;
                float neww11 = fracY * fracX;

                // Interpolate the **value** (stays as float)

                float interpolatedS = neww00 * arrayS[i0][j0] + neww10 * arrayS[i1][j0] + 
                                    neww01 * arrayS[i0][j1] + neww11 * arrayS[i1][j1];

                //int targetI = (int)Math.max(0, Math.min(maxScreenRow - 1, (newX)));
                //int targetJ = (int)Math.max(0, Math.min(maxScreenCol - 1, (newY)));

                newArrayS[i][j] = interpolatedS;
            }
        }

        for(int i = 1; i < maxScreenRow - 1; i++){
            for(int j  = 1; j < maxScreenCol - 1; j++){
                arrayS[i][j] = newArrayS[i][j];
                arrayS[i][j] *= 0.995f; //dissipation factor
            }
        }
    }

    public void injectSmoke(){
        int centerCol = maxScreenCol / 2;
        int startRow = maxScreenRow - 8;
        int endRow = maxScreenRow - 4;

        for(int i = startRow; i < endRow; i++){
            for(int j = centerCol - 2; j < centerCol + 2; j++){
                if(arrayP[i][j] == 0){
                    arrayS[i][j] = 1f;
                    //arrayV[i][j] = -1f; //upward velocity
                    arrayU[i][j] = 0f; //no horizontal velocity
                }

            }
        }
    }

    public void update(double drawInt){
        injectSmoke();
        // for(int i = maxScreenRow/4; i < maxScreenRow - maxScreenRow/4; i++){
        //     for(int j = maxScreenCol; j > maxScreenCol/2; j--){
        //         if(arrayP[i][j] == 1){
        //             arrayS[i][j] = 1f;
        //         }
        //     }
        // }
        updateGravity(drawInt);
        advectVelocity(drawInt);
        addBuoyancy(drawInt);
        clearPressure();
        for(int i = 0; i < 10; i++){
            solvePressure(drawInt);
        }
        updatePressure(drawInt);
        advectDensity(drawInt);




        //FIX ADVECTION BETWEEN CELLS
        //PRESSURE NOT NEEDED BUT COULD BE USED FOR VISUALIZATION OR IMPROVEMENTS LATER

        // for(int i = 1; i < maxScreenRow - 1; i++){
        //     for(int j  = 1; j < maxScreenCol - 1; j++){
        //         System.out.println(newArrayU[i][j] + ", " + newArrayV[i][j]);
        //     }
        // }

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
            float colorValue = Math.clamp(arrayS[i][j], 0, 1);
            int rgb = (int)(colorValue * 255);
            graphics.setColor(new Color(rgb, rgb, rgb));

                graphics.fillRect(j * tileSize, i * tileSize, tileSize, tileSize);
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
