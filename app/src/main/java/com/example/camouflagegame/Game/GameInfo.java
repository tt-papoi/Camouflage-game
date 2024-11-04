package com.example.camouflagegame.Game;

import java.io.Serializable;
import java.util.Arrays;

public class GameInfo implements Serializable {
    public int TYPE = 0;
    public final int SEND_MESSAGE = 0;
    public final int SEND_STARTED_MAP = 1;
    public final int SEND_MAP = 2;
    public String message;
    public int[][] enemyMap;
    public int[][] yourMap;
    public boolean isMyReadyButtonClicked = false;
    public boolean isEnemyReadyButtonClicked = false;
    public boolean goFirst = true;
    public boolean isYourTurn = true;
    public int[] posBullet;
    public GameInfo(){
        posBullet = new int[]{-1, -1};
        yourMap = new int[10][10];
        enemyMap = new int[10][10];
        message = "";
        initTanksPosition();
    }
    public GameInfo(GameInfo info)
    {
        posBullet = new int[]{-1, -1};
        yourMap = new int[10][10];
        enemyMap = new int[10][10];
        message = "";
        initTanksPosition();

        TYPE = info.TYPE;
        message = info.message;
        copyYourMap(info.yourMap);
        copyEnemyMap(info.enemyMap);
        isEnemyReadyButtonClicked = info.isEnemyReadyButtonClicked;
        isMyReadyButtonClicked = info.isMyReadyButtonClicked;
        goFirst = info.goFirst;
        isYourTurn = info.isYourTurn;
        posBullet[0] = info.posBullet[0];
        posBullet[1] = info.posBullet[1];
    }
    public void initTanksPosition()
    {
        // tank1's default position
        yourMap[0][0] = 1;
        yourMap[1][0] = 1;

        // tank2's default position
        yourMap[5][3] = 2;
        yourMap[6][3] = 2;
        yourMap[7][3] = 2;

        // tank3's default position
        yourMap[5][6] = 3;
        yourMap[6][6] = 3;
        yourMap[7][6] = 3;

        // tank4's default position
        yourMap[0][4] = 4;
        yourMap[1][4] = 4;
        yourMap[2][4] = 4;
        yourMap[3][4] = 4;

        // tank5's default position
        yourMap[4][0] = 5;
        yourMap[5][0] = 5;
        yourMap[6][0] = 5;
        yourMap[7][0] = 5;
        yourMap[8][0] = 5;

        // tank1's default position
        enemyMap[0][0] = 1;
        enemyMap[1][0] = 1;

        // tank2's default position
        enemyMap[5][3] = 2;
        enemyMap[6][3] = 2;
        enemyMap[7][3] = 2;

        // tank3's default position
        enemyMap[5][6] = 3;
        enemyMap[6][6] = 3;
        enemyMap[7][6] = 3;

        // tank4's default position
        enemyMap[0][4] = 4;
        enemyMap[1][4] = 4;
        enemyMap[2][4] = 4;
        enemyMap[3][4] = 4;

        // tank5's default position
        enemyMap[4][0] = 5;
        enemyMap[5][0] = 5;
        enemyMap[6][0] = 5;
        enemyMap[7][0] = 5;
        enemyMap[8][0] = 5;
    }
    public boolean isLose()
    {
        int successBullet = 0;
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                if(yourMap[i][j] < 0)
                {
                    successBullet++;
                }
            }
        }
        return successBullet == 17;
    }
    public boolean isWin()
    {
        int successBullet = 0;
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                if(enemyMap[i][j] < 0)
                {
                    successBullet++;
                }
            }
        }
        return successBullet == 17;
    }
    public boolean isReady()
    {
        return isMyReadyButtonClicked && isEnemyReadyButtonClicked;
    }
    public void resetMessage()
    {
        message = "";
    }
    public void resetMap()
    {
        resetEnemyMap();
        resetYourMap();
        initTanksPosition();
    }
    public void resetYourMap()
    {
        for (int[] row: yourMap) {
            Arrays.fill(row, 0);
        }
    }
    public void resetEnemyMap()
    {
        for (int[] row: enemyMap) {
            Arrays.fill(row, 0);
        }
    }
    public void copyYourMap(int[][] map)
    {
        for(int i = 0; i < 10; i++)
        {
            System.arraycopy(map[i], 0, this.yourMap[i], 0, 10);
        }
    }

    public void copyEnemyMap(int[][] map)
    {
        for(int i = 0; i < 10; i++)
        {
            System.arraycopy(map[i], 0, this.enemyMap[i], 0, 10);
        }
    }
    public int[] getRandomBullet()
    {
        int[] pos = new int[]{0,0};
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                if(enemyMap[i][j] >=0 && enemyMap[i][j]<6)
                {
                    pos[0] = i;
                    pos[1] = j;
                    return pos;
                }
            }
        }
        return pos;
    }
}
