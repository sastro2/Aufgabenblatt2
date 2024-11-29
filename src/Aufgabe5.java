import codedraw.*;
import java.awt.*;
import java.util.Scanner;

public class Aufgabe5 {
    static int iterations = 4, windowSideLength = 750, cellCount = 250;
    static float timeStep = 80f, diff = 10f, cellSideLength = (float)windowSideLength / cellCount;

    static float[] flow = new float[cellCount * cellCount];
    static float[] prevFlow = new float[cellCount * cellCount];

    static void addFlow(int x, int y){
        int index = get1DIndex(x,y);
        flow[index] += 1;
    }
    static void diffuse(float[] prevFlow, float[] flow, float rate, float timeStep){
        float a = timeStep * rate * (cellCount - 2) * (cellCount - 2);
        linSolve(prevFlow, flow, a, 6*a+1);
    }
    //Mathemagics dont question it
    static void linSolve(float[] prevFlow, float[] flow, float a, float c){
        float cRecip = 1/c;
        for(int k = 0; k < iterations; k++){
            for(int j = 1; j < cellCount - 1; j++){
                for(int i = 1; i < cellCount - 1; i++){
                    prevFlow[get1DIndex(i,j)] = (flow[get1DIndex(i,j)] + a * (prevFlow[get1DIndex(i+1, j)] + prevFlow[get1DIndex(i-1, j)] + prevFlow[get1DIndex(i, j+1)] + prevFlow[get1DIndex(i, j-1)])) * cRecip;
                }
            }
        }
    }
    static void advect(float[] flow, float[] prevFlow, float timeStep){
        float displacementX = timeStep * (cellCount - 2);
        float displacementY = timeStep * (cellCount - 2);

        //adjacent cell positions
        int i0, i1, j0, j1;
        float distanceFromCenterX, distanceFromCenterY, newXPos, newYPos;

        for(int j = 1; j < cellCount - 1; j++){
            for(int i = 1; i < cellCount - 1; i++){
                distanceFromCenterX = displacementX * flow[get1DIndex(i,j)];
                distanceFromCenterY = displacementY * flow[get1DIndex(i,j)];
                newXPos = i - distanceFromCenterX;
                newYPos = j - distanceFromCenterY;

                if(newXPos < 1) newXPos = 1;
                if(newXPos > cellCount - 2) newXPos = cellCount - 2;
                i0 = (int)Math.floor(newXPos);
                i1 = i0 + 1;
                if(newYPos < 1) newYPos = 1;
                if(newYPos > cellCount - 2) newYPos = cellCount - 2;
                j0 = (int)Math.floor(newYPos);
                j1 = j0 + 1;

                flow[get1DIndex(i,j)] = (prevFlow[get1DIndex(i0, j0)] + prevFlow[get1DIndex(i0, j1)] + prevFlow[get1DIndex(i1, j0)] + prevFlow[get1DIndex(i1, j1)]) / 4;
            }
        }
    }
    static void advanceSim(){
        diffuse(prevFlow, flow, diff, timeStep);
        advect(flow, prevFlow, timeStep);
    }

    static int get1DIndex(int x, int y){
        if(x >= cellCount) x = cellCount - 1;
        if(y >= cellCount) y = cellCount - 1;
        if(x < 0) x = 0;
        if(y < 0) y = 0;

        return x + y * (cellCount);
    }
    static void onMouseDown(int x, int y){
        addFlow(x / (int)cellSideLength, y / (int)cellSideLength);
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println();
        System.out.println("\u001b[36m\u001b[4m\u001b[1mWelcome to the least fluid fluid simulation, to play with the fluid use your mouse and drag it around\u001b[0m");
        System.out.println();
        System.out.println("Please pick a color palette by entering the corresponding letter: BLUE: b, RED: r, GREEN: g");
        String input = scanner.nextLine();
        Color lowFlowColor;
        Color mediumFlowColor;
        Color highFlowColor;

        switch(input){
            case "b": {
                highFlowColor = Palette.BLUE;
                mediumFlowColor = Palette.DARK_TURQUOISE;
                lowFlowColor = Palette.AQUA;
                break;
            }
            case "r": {
                highFlowColor = Palette.RED;
                mediumFlowColor = Palette.CRIMSON;
                lowFlowColor = Palette.LIGHT_CORAL;
                break;
            }
            case "g": {
                highFlowColor = Palette.GREEN;
                mediumFlowColor = Palette.MEDIUM_SEA_GREEN;
                lowFlowColor = Palette.DARK_SEA_GREEN;
                break;
            }
            default:{
                highFlowColor = Palette.BLUE;
                mediumFlowColor = Palette.DARK_TURQUOISE;
                lowFlowColor = Palette.AQUA;
            }
        }

        CodeDraw window = new CodeDraw(windowSideLength, windowSideLength);

        boolean mouseDown = false;
        while(!window.isClosed()){
            for (var e : window.getEventScanner()) {
                switch (e) {
                    case MouseUpEvent mouseUpEvent ->
                        mouseDown = false;
                    case MouseDownEvent mouseDownEvent ->{
                        mouseDown = true;
                        onMouseDown(mouseDownEvent.getX(), mouseDownEvent.getY());
                    }
                    case MouseMoveEvent mouseMoveEvent -> {
                        if (mouseDown) onMouseDown(mouseMoveEvent.getX(), mouseMoveEvent.getY());
                    }
                    default -> { }
                }
            }

            advanceSim();
            for(int i = 0; i < cellCount; i++){
                for(int j = 0; j < cellCount; j++){
                    float x = i * cellSideLength;
                    float y = j * cellSideLength;
                    float currentFlow = flow[get1DIndex(i, j)];

                    if(currentFlow < 1.0E-40) window.setColor(lowFlowColor);
                    else if(currentFlow < 1.0E-20) window.setColor(mediumFlowColor);
                    else if(currentFlow < 1.0E-10) window.setColor(highFlowColor);

                    window.fillSquare(x, y, cellSideLength);
                }
            }
            window.show((long)timeStep);
        }
    }
}
