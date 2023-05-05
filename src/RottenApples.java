import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;


public class RottenApples {
    //Apple object encapsulate a Rectangle for its position and size and a spawnApple
    //behavior for when the snake has eaten the apple, getters, and setters
    private Rectangle rectangle;

    public RottenApples()
    {
        rectangle = new Rectangle(0,0,Constants.SIZE, Constants.SIZE);
        spawnRottenApples();

    }

    public void spawnRottenApples()
    {

        int randNumX = (int)(Math.random() * 20);
        int randNumY = (int)(Math.random() * 15);
        randNumX *= 20;
        randNumY *= 20;
        rectangle.x = randNumX;
        rectangle.y = randNumY;

        //TODO: generate a random x and y that lands on a bottom left corner of the grid
        //and then assign rectangle.x and rectangle.y to those values

        //DELETE the 2 lines below once you complete the above
        // rectangle.x = Constants.SIZE * 5;
        // rectangle.y = Constants.SIZE * 5;
        //you can modify this even more to have a parameter of type Array<SnakeBody>
        //to make sure it does not spawn on the snake

    }

    public Rectangle getRectangle()
    { return rectangle; }

    public void setX(float x)
    { rectangle.x = x; }

    public void setY(float y)
    { rectangle.y = y; }
}
