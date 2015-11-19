package ratclient;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

@SuppressWarnings("serial")
public class ConsoleCaret extends DefaultCaret {

  public ConsoleCaret() {
    setBlinkRate(500); //blink ever 1/2 sec
  }

  protected synchronized void damage(Rectangle r) { //removes the current caret painting, re paints it
    if (r == null) {
    	return;
    }
    x = r.x;
    y = r.y + (r.height * 4 / 5 - 3);
    width = 8;
    height = 8;
    repaint();
  }

  public void paint(Graphics g) {//draw caret
    JTextComponent comp = getComponent();
    if (comp == null) {
      return;
    }

    int dot = getDot();
    Rectangle r = null;
    try {
      r = comp.modelToView(dot);
    } catch (BadLocationException e) {
      return;
    }
    if (r == null) {
      return;
    }

    int dist = r.height * 4 / 5 - 3; 

    if ((x != r.x) || (y != r.y + dist)) { 
    	repaint();
    	x = r.x; 
    	y = r.y + dist;
    	width = 8;
    	height = 8;
    }
    
    if(isVisible()) {
    	g.setColor(comp.getCaretColor());
        g.drawLine(r.x, r.y + dist + 4,r.x + 6, r.y + dist + 4); //Draw caret underline
    }
  }
}