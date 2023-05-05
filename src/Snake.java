
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
public class Snake extends ApplicationAdapter
{
    private ShapeRenderer renderer;//used to draw textures
    private OrthographicCamera camera;//the camera to our world
    private Viewport viewport;//maintains the ratios of your world
    private BitmapFont font; //used to draw fonts (text)
    private SpriteBatch batch; //also needed to draw fonts (text)
    private GlyphLayout layout;

    //abtract data types to represent different parts of the game
    private SnakeHead snakeHead;
    private Apple apple;
    private Rock rock;
    private RottenApples rotten;
    private Array<SnakePart> snakeBody;

    private int score;

    //Preferences allows us to save a high score
    private Preferences preferences;
    //random image to use for the snake head, feel free to take this out
    //or add images for the snake body as well
    private Texture snakeHeadPic;
    private Texture snake2;

    private Vector2 mousePos;
    private Circle circleMenu;
    private static final int RADIUS2 = 35;
    private static final float CENTER_X = Constants.WORLD_WIDTH / 2;
    private static final float CENTER_Y = Constants.WORLD_HEIGHT / 2;
    private boolean menu;
    private boolean game;
    private int ctr;

    @Override//called once when the game is started (kind of like our constructor)
    public void create(){
        //initialize our instance variables
        renderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        //This project uses a Constants class to keep track of all the constants
        //They are public so they can be used in any class in our project
        //To access a public constant in the Constants class you use: Constant.YOUR_CONSTANT
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        font = new BitmapFont();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        apple = new Apple();
        rock = new Rock();
        rotten = new RottenApples();
        snakeHead = new SnakeHead();
        snakeBody = new Array<SnakePart>();

        score = 0;

        preferences = Gdx.app.getPreferences("High Scores");
        //if there has not been a high score yet set it to 0
        if(!preferences.contains("HighScore"))
        {
            preferences.putInteger("HighScore", 0);
        }
        preferences.flush();//this saves the high score

        //intialize the picture you want to use
        snakeHeadPic = new Texture(Gdx.files.internal("assets/SnakeHead.png"));
        circleMenu = new Circle(CENTER_X, CENTER_Y,RADIUS2);
        mousePos = new Vector2();
        snake2 = new Texture("snake2.png");
        menu = true;
        game = false;
        ctr = 0;
    }

    @Override//this is called 60 times a second, all the drawing is in here, or helper
    //methods that are called from here
    public void render(){
        int x1 = Gdx.input.getX();
        int y1 = Gdx.input.getY();
        mousePos = viewport.unproject(new Vector2(x1, y1));

        viewport.apply();

        //these two lines wipe and reset the screen so when something action had happened
        //the screen won't have overlapping images
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(game) {

            //update state of our data types
            snakeHead.update();//calls the Update in the SnakeHead class,
            //which checks if an arrow key has been pressed to change the
            //direction of the snake
            snakeHead.move(snakeBody);//calls the move method in the SnakeHead class
            //and passes in the snakeBody
            checkCollisionApple();
            checkCollisionRock();
            checkCollisionRottenApples();
            //did the snake collide with an apple
            checkCollisionBody(); //did the snake collide with itself
            checkCollisionWalls();//did the snake collide with a wall
            checkHighScore(); //did we get a new high score

            //AFTER everything is updated, draw everything based on the new state
            //render
            drawGrid();

            renderer.setColor(Color.WHITE);
            renderSnakeHead();
            renderSnakeBody();
            renderApple();
            renderRock();
            renderRottenApple();

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            font.draw(batch, "Score: " + score, 20, Constants.WORLD_HEIGHT - 20);
            font.draw(batch, "HighScore: " + preferences.getInteger("HighScore"), Constants.WORLD_WIDTH - 110, Constants.WORLD_HEIGHT - 20);
            batch.end();
        }
        else if(menu) {
            batch.begin();
            batch.draw(snake2, 0, 0);
            batch.end();
            Circle tempMouse = new Circle(mousePos.x, mousePos.y, 1);

            if(Intersector.overlaps(tempMouse, circleMenu)) {

                renderer.setColor(new Color(126/255f, 148/255f, 139/255f, 1f));
                font.setColor(Color.PINK);
                if(Gdx.input.justTouched())//if we click start the game
                {
                    game = true;
                    menu = false;

                }

            }
            else {
                renderer.setColor(new Color(50/255f, 168/255f, 166/255f, 1f));
                font.setColor(new Color(50/255f, 168/255f, 166/255f, 1f));
            }
            renderer.setProjectionMatrix(viewport.getCamera().combined);
            renderer.begin(ShapeType.Filled);
            renderer.setColor(new Color(50/255f, 168/255f, 166/255f, .5f));
            renderer.circle(circleMenu.x, circleMenu.y, circleMenu.radius);

            renderer.end();

            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();

            layout.setText(font, "START!");
            font.draw(batch, layout, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT / 2 + layout.height / 2);

            batch.end();

        }
    }

    private void renderSnakeBody()
    {
        for(int i = 0; i < snakeBody.size; i++)
        {
            //Draw each snake body part
            SnakePart s = snakeBody.get(i);
            Rectangle rectangle = s.getRectangle();
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setColor(Color.BLUE);
            renderer.rect(rectangle.x, rectangle.y, Constants.SIZE, Constants.SIZE);
            renderer.end();

            //draw a border around each snake body part
            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(Color.WHITE);
            renderer.rect(rectangle.x, rectangle.y, Constants.SIZE, Constants.SIZE);
            renderer.end();
        }
    }

    private void renderApple()
    {
        //draw the apple
        Rectangle rectangle = apple.getRectangle();
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.RED);
        renderer.rect(rectangle.x, rectangle.y, Constants.SIZE, Constants.SIZE);
        renderer.end();
    }

    private void renderSnakeHead()
    {
        //draw the image of the snake's head
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(snakeHeadPic,
                snakeHead.getX(),
                snakeHead.getY(),
                Constants.SIZE,
                Constants.SIZE);
        batch.end();
    }

    private void checkCollisionApple()
    {
        //The Rectangle class in libgdx has an overlaps method
        //to see if two rectangles have overlapped
        if(snakeHead.getRectangle().overlaps(apple.getRectangle()))
        {
            //spawn new apple
            apple.spawnApple();
            rock.spawnRock();
            rotten.spawnRottenApples();

            //add to snakebody
            //set the last position to either the last snake body part or the snake head,
            //if there are not snake body parts yet
            Rectangle last;
            if(snakeBody.size != 0)
                last = snakeBody.get(snakeBody.size - 1).getRectangle();
            else
                last = snakeHead.getRectangle();

            //depending on the direction of the snake add a snake body part in the correct
            //position, remember for Rectangle objects the (x,y) position is in the
            //bottom left
            if(snakeHead.getDirection() == Direction.RIGHT)
                snakeBody.add(new SnakePart(last.x - Constants.SIZE, last.y ));
            if(snakeHead.getDirection() == Direction.LEFT)
                snakeBody.add(new SnakePart(last.x + Constants.SIZE, last.y));
            if(snakeHead.getDirection() == Direction.UP)
                snakeBody.add(new SnakePart(last.x, last.y - Constants.SIZE));
            if(snakeHead.getDirection() == Direction.DOWN)
                snakeBody.add(new SnakePart(snakeHead.getX(), snakeHead.getY() + Constants.SIZE));

            score++;//don't forget to increase the score
        }
    }

    private void renderRottenApple()
    {
        //draw the apple
        Rectangle rectangle = rotten.getRectangle();
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.GREEN);
        renderer.rect(rectangle.x, rectangle.y, Constants.SIZE, Constants.SIZE);
        renderer.end();
    }

    private void checkCollisionRottenApples()
    {
        //The Rectangle class in libgdx has an overlaps method
        //to see if two rectangles have overlapped
        if(snakeHead.getRectangle().overlaps(rotten.getRectangle()))
        {
            //spawn new apple

            snakeBody.clear();

            score = 0;//don't forget to decrease the score
        }
    }

    private void renderRock()
    {
        //draw the rock
        Rectangle rectangle = rock.getRectangle();
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.GRAY);
        renderer.rect(rectangle.x, rectangle.y, Constants.SIZE, Constants.SIZE);
        renderer.end();
    }

    private void checkCollisionRock()
    {
        //The Rectangle class in libgdx has an overlaps method
        //to see if two rectangles have overlapped
        if(snakeHead.getRectangle().overlaps(rock.getRectangle()))
        {

            gameOver();

            // //spawn new rock
            // rock.spawnRock();

            // //add to snakebody
            // //set the last position to either the last snake body part or the snake head,
            // //if there are not snake body parts yet
            // Rectangle last;
            // if(snakeBody.size != 0)
            // last = snakeBody.get(snakeBody.size - 1).getRectangle();
            // else
            // last = snakeHead.getRectangle();

            // //depending on the direction of the snake add a snake body part in the correct
            // //position, remember for Rectangle objects the (x,y) position is in the
            // //bottom left
            // if(snakeHead.getDirection() == Direction.RIGHT)
            // snakeBody.add(new SnakePart(last.x - Constants.SIZE, last.y ));
            // if(snakeHead.getDirection() == Direction.LEFT)
            // snakeBody.add(new SnakePart(last.x + Constants.SIZE, last.y));
            // if(snakeHead.getDirection() == Direction.UP)
            // snakeBody.add(new SnakePart(last.x, last.y - Constants.SIZE));
            // if(snakeHead.getDirection() == Direction.DOWN)
            // snakeBody.add(new SnakePart(snakeHead.getX(), snakeHead.getY() + Constants.SIZE));

            // score++;//don't forget to increase the score
        }

    }

    private void checkCollisionBody()
    {
        //check if the snakeHead has collided with itself
        for(SnakePart p : snakeBody)
        {
            if(p.getRectangle().overlaps(snakeHead.getRectangle()))
            {
                gameOver();//call helper method gameOver() if this has happened
            }
        }
    }

    private void checkCollisionWalls()
    {
        //Check collision with walls, which depends on direction
        if(snakeHead.getDirection() == Direction.RIGHT && snakeHead.getX() + Constants.SIZE > Constants.WORLD_WIDTH)
            gameOver();
        if(snakeHead.getDirection() == Direction.LEFT && snakeHead.getX() < 0)
            gameOver();
        if(snakeHead.getDirection() == Direction.UP && snakeHead.getY() + Constants.SIZE > Constants.WORLD_HEIGHT)
            gameOver();
        if(snakeHead.getDirection() == Direction.DOWN && snakeHead.getY() < 0)
            gameOver();
    }

    private void checkHighScore()
    {
        //do we need to save a new high score
        if(score > preferences.getInteger("HighScore"))
        {
            preferences.putInteger("HighScore", score);
            preferences.flush();
        }
    }

    private void drawGrid()
    {
        renderer.setProjectionMatrix(camera.combined);

        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.BLUE);

        //draw the horizontal lines
        for(int i = 0; i < Constants.WORLD_HEIGHT; i+= Constants.SIZE)
        {
            renderer.line(0, i, Constants.WORLD_WIDTH, i);
        }
        //draw the vertical lines
        for(int i = 0; i < Constants.WORLD_WIDTH; i+= Constants.SIZE)
        {
            renderer.line(i, 0, i, Constants.WORLD_HEIGHT);
        }
        renderer.end();
    }

    public void gameOver()
    {
        //if the game is over reset the game
        snakeBody.clear();
        score = 0;
        snakeHead.setX(0);
        snakeHead.setY(0);
        snakeHead.setDirection(Direction.RIGHT);
        apple.spawnApple();
        rock.spawnRock();
        ctr = 0;
        game = false;
        menu = true;

    }
    @Override
    public void dispose () {
        renderer.dispose();
        batch.dispose();
        font.dispose();
    }

    @Override
    public void resize (int width, int height) {
        viewport.update(width, height, true);
    }

}
