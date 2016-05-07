package me.android.jinqiang.gomoku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * @author  yanjinqiang on 2016/5/5.
 */
public class GomokuPanel extends View{
    private int mPanelWidth;
    private float mLineHeight;
    private int MAX_LINE = 10; //表示10行
    private Paint mPaint = new Paint();
    private Bitmap mWhitePiece; //白色棋子
    private Bitmap mBlackPiece;//黑色棋子
    private float ratioPieceOfLineHeight = 3*1.0f / 4;  //棋子相对于单行高度的3/4.
    private ArrayList<Point> mWhitePoint = new ArrayList<>();
    private ArrayList<Point> mBlackPoint = new ArrayList<>();
    private boolean isWhite = true; //表示当前是白棋。
    private boolean isGG = false;
    private boolean isWhiteWinner = true;
    private static String INSTANCE ="instance"; //当前执手是什么颜色的棋
    private static String INSTANCE_GAME_RESULT = "instance_game_result";//胜负结果
    private static String WHITE_ARRAY = "white_array";//白旗数组
    private static String BLACK_ARRAY = "black_array";//黑棋数组



    public GomokuPanel(Context context) {
        super(context);
    }

    public GomokuPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0x330000ff);
        initPaint(context);
    }

    public GomokuPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(0x330000ff);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height  = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode  = MeasureSpec.getMode(heightMeasureSpec);

        if(widthMode == MeasureSpec.UNSPECIFIED){
                 width = height;
        }else if(heightMode == MeasureSpec.UNSPECIFIED){
            height = width;
        }
        width = Math.min(width,height);
        //宽高一致，取最小值。
        setMeasuredDimension(width,width);

    }

    private void initPaint(Context context){
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);//绘制直线
        mPaint.setDither(true);
//        ShapeDrawable circleWhite = new ShapeDrawable(new OvalShape());
//        ShapeDrawable circleBlack = new ShapeDrawable(new OvalShape());
//        circleWhite.getPaint().setColor(Color.WHITE);
//        circleBlack.getPaint().setColor(Color.BLACK);
        Drawable drawable = new ShapeDrawable(new OvalShape());
//        BitmapDrawable bdBlack = (BitmapDrawable)circleBlack.getCurrent();
//        BitmapDrawable bdWhite = (BitmapDrawable)circleWhite.getCurrent();

        mWhitePiece = BitmapFactory.decodeResource(getResources(),R.drawable.android);

        mBlackPiece = BitmapFactory.decodeResource(getResources(),R.drawable.apple);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //棋盘的宽度取屏幕宽
        mPanelWidth = w;
        mLineHeight = (mPanelWidth * 1.0f)/MAX_LINE;

        int pieceSize = (int) (mLineHeight*ratioPieceOfLineHeight);
        //棋子的大小是每行高度的3/4，这样可以预留出空闲的地方。
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece,pieceSize,pieceSize,false);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece,pieceSize,pieceSize,false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);
        drawPieces(canvas);
        checkGameOver();

    }

    /**
     * 绘制棋子
     * @param canvas
     */
    private void drawPieces(Canvas canvas) {
        for(int i = 0,n = mWhitePoint.size();i < n;i ++){
            Point point = mWhitePoint.get(i);
            canvas.drawBitmap(mWhitePiece,
                    (point.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    (point.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,null);
        }
        for(int i = 0,n = mBlackPoint.size();i < n;i ++){
            Point point = mBlackPoint.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (point.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    (point.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,null);
        }
    }

    /**
     * 绘制棋盘
     * @param canvas
     */
    private void drawBoard(Canvas canvas){
        int w = mPanelWidth;
        float lineHeight  = mLineHeight;
        for(int i = 0;i < MAX_LINE;i ++){
            int startX = (int) (lineHeight/2);
            int endX = (int) (w - lineHeight / 2);
            int startY = (int) ((0.5 + i)*lineHeight);
            canvas.drawLine(startX,startY,endX,startY,mPaint);//绘制横线
            canvas.drawLine(startY,startX,startY,endX,mPaint); //竖线
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isGG) return false;
        if(event.getAction() == MotionEvent.ACTION_UP){
            //按下抬起后，绘制棋子的位置.
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point point = getPointOnPanel(x,y);
            if(mWhitePoint.contains(point) || mBlackPoint.contains(point)) {
                return false;
            }
            if(isWhite)
                mWhitePoint.add(point);
            else
                mBlackPoint.add(point);
            isWhite = !isWhite;
            invalidate();
            return true;
        }
        return true;
    }
    public Point getPointOnPanel(int x,int y){

        return new Point((int)(x / mLineHeight), (int) (y / mLineHeight));

    }

    private void checkGameOver(){
        if(checkFiveInLine(mBlackPoint)){
            isWhiteWinner = false;
            isGG = true;
        }else if(checkFiveInLine(mWhitePoint)){
            isWhiteWinner = true;
            isGG = true;
        }else
            isGG = false;
        String text ="五子连珠,";
        if(isGG){
            text += isWhiteWinner? "安卓胜利" :"苹果胜利";
            Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();
        }

    }
    private boolean checkFiveInLine(ArrayList<Point> points){
        for(Point point : points){
            int x = point.x;
            int y = point.y;
            if(checkHorizontal(x,y,points)
                    || checkVertical(x,y,points)
                    ||checkLeftDiagonal(x,y,points)
                    || checkRightDiagonal(x,y,points)){
                return true;
            }
        }
        return false;
    }

    /**
     * 比较方法，查看横向是否存在五子连珠
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x,int y,ArrayList<Point> points){
        int count = 1;
        for(int i = 1; i <= 5;i ++ ){
            if(points.contains(new Point(x - i,y)))
                count ++ ;
            else
                break;
        }
        if(count >= 5)
            return true;
        for(int i = 1; i <= 5;i ++ ){
            if(points.contains(new Point(x + i,y)))
                count ++ ;
            else
                break;
        }
        if(count >= 5)
            return true;
        return false;
    }
    private boolean checkVertical(int x,int y,ArrayList<Point> points){
        int count = 1;
        for(int i = 1; i <= 5;i ++ ){
            if(points.contains(new Point(x,y - i)))
                count ++ ;
            else
                break;
        }
        if(count >= 5)
            return true;
        for(int i = 1; i <= 5;i ++ ){
            if(points.contains(new Point(x,y + i)))
                count ++ ;
            else
                break;
        }
        if(count >= 5)
            return true;
        return false;
    }
    private boolean checkLeftDiagonal(int x,int y,ArrayList<Point> points){
        int count = 1;
        for(int i = 1; i <= 5;i ++ ){
            if(points.contains(new Point(x - i,y + i)))
                count ++ ;
            else
                break;
        }
        if(count >= 5)
            return true;
        for(int i = 1; i <= 5;i ++ ){
            if(points.contains(new Point(x + i,y - i)))
                count ++ ;
            else
                break;
        }
        if(count >= 5)
            return true;
        return false;
    }

    private boolean checkRightDiagonal(int x,int y,ArrayList<Point> points){
        int count = 1;
        for(int i = 1; i <= 5;i ++ ){
            if(points.contains(new Point(x + i,y + i)))
                count ++ ;
            else
                break;
        }
        if(count >= 5)
            return true;
        for(int i = 1; i <= 5;i ++ ){
            if(points.contains(new Point(x - i,y - i)))
                count ++ ;
            else
                break;
        }
        if(count >= 5)
            return true;
        return false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_RESULT,isGG);
        bundle.putParcelableArrayList(WHITE_ARRAY,mWhitePoint);
        bundle.putParcelableArrayList(BLACK_ARRAY,mBlackPoint);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle){
            Bundle bundle = (Bundle)state;
            mWhitePoint = bundle.getParcelableArrayList(WHITE_ARRAY);
            mBlackPoint = bundle.getParcelableArrayList(BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return ;
        }
        super.onRestoreInstanceState(state);
    }
}
