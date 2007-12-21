package haven;

import java.awt.Canvas;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Graphics;
import java.util.*;

public class HavenPanel extends Canvas implements Runnable {
	RootWidget root;
	UI ui;
	int w, h;
	List<InputEvent> events = new LinkedList<InputEvent>();
	
	public HavenPanel(int w, int h) {
		setSize(this.w = w, this.h = h);
	}
	
	public void init() {
		setFocusTraversalKeysEnabled(false);
		createBufferStrategy(2);
		root = new RootWidget(new Coord(w, h), getGraphicsConfiguration());
		ui = new UI(root);
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}

			public void keyPressed(KeyEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}
			public void keyReleased(KeyEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}

			public void mouseMoved(MouseEvent e) {
				synchronized(events) {
					events.add(e);
				}
			}
});
	}
	
	void redraw() {
		BufferStrategy bs = getBufferStrategy();
		Graphics g = bs.getDrawGraphics();
		try {
			root.draw(g);
		} finally {
			g.dispose();
		}
		bs.show();
	}
	
	void dispatch() {
		synchronized(events) {
			while(events.size() > 0) {
				InputEvent e = events.remove(0);
				if(e instanceof MouseEvent) {
					MouseEvent me = (MouseEvent)e;
					if(me.getID() == MouseEvent.MOUSE_PRESSED) {
						ui.mousedown(new Coord(me.getX(), me.getY()), me.getButton());
					} else if(me.getID() == MouseEvent.MOUSE_RELEASED) {
						ui.mouseup(new Coord(me.getX(), me.getY()), me.getButton());
					} else if(me.getID() == MouseEvent.MOUSE_MOVED || me.getID() == MouseEvent.MOUSE_DRAGGED) {
						ui.mousemove(new Coord(me.getX(), me.getY()));
					}
				} else if(e instanceof KeyEvent) {
					KeyEvent ke = (KeyEvent)e;
					if(ke.getID() == KeyEvent.KEY_PRESSED) {
						ui.keydown(ke);
					} else if(ke.getID() == KeyEvent.KEY_RELEASED) {
						ui.keyup(ke);
					} else if(ke.getID() == KeyEvent.KEY_TYPED) {
						ui.type(ke);
					}
				}
			}
		}
	}
	
	public void run() {
		try {
			while(true) {
				long now, then;
				then = System.currentTimeMillis();
				try {
					if(Session.current != null)
						Session.current.oc.tick();
					synchronized(ui) {
						dispatch();
						redraw();
					}
				} catch(Throwable t) {
					t.printStackTrace();
					throw(new Error(t));
				}
				now = System.currentTimeMillis();
				//System.out.println(now - then);
				if(now - then < 60) {
					Thread.sleep(60 - (now - then));
				}
			}
		} catch(InterruptedException e) {}
	}
}