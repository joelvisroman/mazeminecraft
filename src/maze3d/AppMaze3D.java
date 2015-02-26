package maze3d;

import static javax.media.opengl.GL2.GL_COMPILE;
import static javax.media.opengl.GL2GL3.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT1;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_POSITION;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;

public class AppMaze3D implements GLEventListener, MouseListener,
		MouseMotionListener, KeyListener {

	private static final int FPS = 60;
	private GL2 gl;
	private GLAutoDrawable glDrawable;
	private GLU glut;

	private int width, height, x, dx, ox, y, dy, oy;

	private boolean frente, volta, esquerda, direita, salta, voltasalta;
	private float posX = 2.5f, posY = 0.3f, posZ = 1.5f;
	private int newposX, newposZ;
	private float eyeX = 0, eyeY = 0, eyeZ = 0.0f;
	private float cenX = 0.0f, cenY = 0.0f, cenZ = 0.0f;
	private float upX = 0.0f, upY = 0.0f, upZ = 0.0f;
	private float azimut = 180f;
	private float zenit = 0f;

	private Texture[] texture = new Texture[3];
	private File[] textureFileName = { new File("wall.jpg"), new File("7.jpg") };
	private float[] textureTop = new float[3];
	private float[] textureBottom = new float[3];
	private float[] textureLeft = new float[3];
	private float[] textureRight = new float[3];

	private List<CuboID> cuboids;

	private final static int C_LENGTH = 1;
	private final static int C_WIDTH = 1;
	private final static int C_HEIGHT = 2;

	private int[][] maze = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 0, 1, 0, 0, 0, 0, 1, 1 }, { 1, 0, 0, 1, 0, 1, 1, 0, 1, 1 },
			{ 1, 0, 1, 1, 0, 0, 1, 0, 1, 1 }, { 1, 0, 0, 0, 1, 0, 1, 0, 0, 0 },
			{ 1, 0, 1, 1, 0, 0, 1, 1, 1, 1 }, { 1, 0, 0, 0, 0, 1, 0, 0, 0, 1 },
			{ 1, 1, 0, 1, 1, 1, 0, 1, 0, 1 }, { 1, 0, 0, 0, 0, 0, 0, 1, 0, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };

	public void init(GLAutoDrawable drawable) {
		glDrawable = drawable;
		gl = glDrawable.getGL().getGL2();
		glut = new GLU();
		gl.glEnable(GL2.GL_DEPTH_TEST);
		try {
			texture[0] = TextureIO.newTexture(textureFileName[0], false);
			texture[1] = TextureIO.newTexture(textureFileName[1], false);

			// texturas a volta dos cubos.
			TextureCoords[] textureCoords = { texture[0].getImageTexCoords(),
					texture[1].getImageTexCoords() };
			textureTop[0] = textureCoords[0].top();
			textureBottom[0] = textureCoords[0].bottom();
			textureLeft[0] = textureCoords[0].left();
			textureRight[0] = textureCoords[0].right();

			textureTop[1] = textureCoords[1].top();
			textureBottom[1] = textureCoords[1].bottom();
			textureLeft[1] = textureCoords[1].left();
			textureRight[1] = textureCoords[1].right();

		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// tentar tratar a luz.
		float[] lightAmbientValue = { 1.0f, 0.0f, 0.0f, 1.0f };
		float[] lightDiffuseValue = { 1.0f, 0.0f, 0.0f, 1.0f };
		float lightDiffusePosition[] = { 10.0f, 6f, 10.0f, 6.0f };

		gl.glLightfv(GL_LIGHT1, GL_AMBIENT, lightAmbientValue, 0);
		gl.glLightfv(GL_LIGHT1, GL_DIFFUSE, lightDiffuseValue, 0);
		gl.glLightfv(GL_LIGHT1, GL_POSITION, lightDiffusePosition, 0);

		gl.glEnable(GL_LIGHT1);

		// Cria um tipo de lista pra gerar as paredes
		gl.glNewList(1, GL_COMPILE);
		drawMaze(maze, gl);
		gl.glEndList();
	}

	public void display(GLAutoDrawable drawable) {

		glDrawable = drawable;

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL2.GL_DEPTH_TEST);

		gl.glFrontFace(GL2.GL_CCW);

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		// como vai ser viasualizado.
		glut.gluPerspective(90, width / (float) height, 0.1f, 20000.0f);

		zenit -= dy;

		if (zenit > 90) {

			zenit = 90;
		}

		if (zenit < -90) {
			zenit = -90;
		}
		azimut += dx;
		azimut = azimut % 360;

		dy = 0;
		dx = 0;

		if (frente) {

			posZ -= (float) ((Math.cos(azimut * Math.PI / 180) * Math.cos(zenit
					* Math.PI / 180))) / 50;
			posX += (float) ((Math.sin(azimut * Math.PI / 180) * Math.cos(zenit
					* Math.PI / 180))) / 50;

			checkCD();
		}

		if (volta) {
			posZ += (float) ((Math.cos(azimut * Math.PI / 180) * Math.cos(zenit
					* Math.PI / 180))) / 50;
			posX -= (float) ((Math.sin(azimut * Math.PI / 180) * Math.cos(zenit
					* Math.PI / 180))) / 50;

			checkCD();
		}
		if (esquerda) {
			posZ -= (float) ((Math.cos(azimut * Math.PI / 180 - Math.PI / 2))) / 50;
			posX += (float) ((Math.sin(azimut * Math.PI / 180 - Math.PI / 2))) / 50;

			checkCD();
		}
		if (direita) {
			posZ += (float) ((Math.cos(azimut * Math.PI / 180 - Math.PI / 2))) / 50;
			posX -= (float) ((Math.sin(azimut * Math.PI / 180 - Math.PI / 2))) / 50;

			checkCD();
		}

		if (salta) {
			posY += 0.1;
		}

		if (voltasalta) {
			posY -= 0.1;
		}

		eyeZ = -(float) (Math.cos(azimut * Math.PI / 180) * Math.cos(zenit
				* Math.PI / 180));
		eyeX = (float) (Math.sin(azimut * Math.PI / 180) * Math.cos(zenit
				* Math.PI / 180));
		eyeY = (float) (Math.sin(zenit * Math.PI / 180));

		upZ = -(float) (Math.cos(azimut * Math.PI / 180) * Math.cos(zenit
				* Math.PI / 180 + Math.PI / 2));
		upX = (float) (Math.sin(azimut * Math.PI / 180) * Math.cos(zenit
				* Math.PI / 180 + Math.PI / 2));
		upY = (float) (Math.sin(zenit * Math.PI / 180 + Math.PI / 2));

		cenX = posX + eyeX * 100;
		cenY = posY + eyeY * 100;
		cenZ = posZ + eyeZ * 100;

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		// onde a camera esta vendo
		glut.gluLookAt(posX, posY, posZ, cenX, cenY, cenZ, upX, upY, upZ);

		texture[1].enable(gl);

		drawXYZ(gl);

		texture[0].enable(gl);
		texture[0].bind(gl);
		gl.glCallList(1);

		texture[1].enable(gl);
		texture[1].bind(gl);

		drawwall();
	}

	private void checkCD() {
		newposZ = (int) posZ;
		newposX = (int) posX;

		// se esta na matriz
		if (newposZ >= 0 && newposZ < maze.length && newposX >= 0
				&& newposX < maze[0].length) {

			// so testa se for 1 na matriz, zero é um espaço pra andar
			if (maze[newposZ][newposX] == 1) {
				// nova posicao + 1 pra saber se realmente e colisão
				if (posZ >= newposZ && posZ <= newposZ + 1) {

					if (posZ - newposZ < 0.5)
						posZ -= 0.05f;
					else
						posZ += 0.05f;
				}

				if (posX >= newposX && posX <= newposX + 1) {
					if (posX - newposX < 0.5) {
						posX -= 0.05f;
					} else {
						posX += 0.05f;
					}
				}
			}
		}
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		this.width = width;
		this.height = height;
	}

	public void dispose(GLAutoDrawable arg0) {
	}

	public static void main(String[] args) {
		JFrame f = new JFrame("N4");

		GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);

		GLCanvas canvas = new GLCanvas(capabilities);
		AppMaze3D ren = new AppMaze3D();
		canvas.addGLEventListener(ren);
		canvas.addMouseListener(ren);
		canvas.addMouseMotionListener(ren);
		canvas.addKeyListener(ren);
		canvas.setSize(1024, 768);

		f.add(canvas);
		// precisa usar pra não ficar estatico.
		final FPSAnimator animator = new FPSAnimator(canvas, FPS, false);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		canvas.requestFocus();
		// precisa usar pra não ficar estatico.
		animator.start();
	}

	// Cria os cubos.
	private void drawMaze(int[][] maze, GL2 gl) {

		for (int row = 0; row < maze.length; row++) {
			for (int col = 0; col < maze[0].length; col++) {

				if (maze[row][col] == 1) {

					// chão
					Vec3D topLeftBot = new Vec3D(C_LENGTH * col, 0, C_WIDTH
							* row);
					Vec3D botLeftBot = new Vec3D(C_LENGTH * col, 0, C_WIDTH
							* row + C_WIDTH);
					Vec3D botRightBot = new Vec3D(C_LENGTH * col + C_LENGTH, 0,
							C_WIDTH * row + C_WIDTH);
					Vec3D topRightBot = new Vec3D(C_LENGTH * col + C_LENGTH, 0,
							C_WIDTH * row);

					Rect squareBot = new Rect(topLeftBot, botLeftBot,
							botRightBot, topRightBot);

					// top
					Vec3D topLeftTop = new Vec3D(C_LENGTH * col, C_HEIGHT,
							C_WIDTH * row);
					Vec3D botLeftTop = new Vec3D(C_LENGTH * col, C_HEIGHT,
							C_WIDTH * row + C_WIDTH);
					Vec3D botRightTop = new Vec3D(C_LENGTH * col + C_LENGTH,
							C_HEIGHT, C_WIDTH * row + C_WIDTH);
					Vec3D topRightTop = new Vec3D(C_LENGTH * col + C_LENGTH,
							C_HEIGHT, C_WIDTH * row);

					Rect squareTop = new Rect(topLeftTop, botLeftTop,
							botRightTop, topRightTop);

					// front
					Vec3D topLeftFro = new Vec3D(C_LENGTH * col, C_HEIGHT,
							C_WIDTH * row + C_WIDTH);
					Vec3D botLeftFro = new Vec3D(C_LENGTH * col, 0, C_WIDTH
							* row + C_WIDTH);
					Vec3D botRightFro = new Vec3D(C_LENGTH * col + C_LENGTH, 0,
							C_WIDTH * row + C_WIDTH);
					Vec3D topRightFro = new Vec3D(C_LENGTH * col + C_LENGTH,
							C_HEIGHT, C_WIDTH * row + C_WIDTH);

					Rect squareFro = new Rect(topLeftFro, botLeftFro,
							botRightFro, topRightFro);

					Vec3D topLeftBac = new Vec3D(C_LENGTH * col + C_LENGTH,
							C_HEIGHT, C_WIDTH * row);
					Vec3D botLeftBac = new Vec3D(C_LENGTH * col + C_LENGTH, 0,
							C_WIDTH * row);
					Vec3D botRightBac = new Vec3D(C_LENGTH * col, 0, C_WIDTH
							* row);
					Vec3D topRightBac = new Vec3D(C_LENGTH * col, C_HEIGHT,
							C_WIDTH * row);

					Rect squareBac = new Rect(topLeftBac, botLeftBac,
							botRightBac, topRightBac);

					// left
					Vec3D topLeftLef = new Vec3D(C_LENGTH * col, C_HEIGHT,
							C_WIDTH * row);
					Vec3D botLeftLef = new Vec3D(C_LENGTH * col, 0, C_WIDTH
							* row);
					Vec3D botRightLef = new Vec3D(C_LENGTH * col, 0, C_WIDTH
							* row + C_WIDTH);
					Vec3D topRightLef = new Vec3D(C_LENGTH * col, C_HEIGHT,
							C_WIDTH * row + C_WIDTH);

					Rect squareLef = new Rect(topLeftLef, botLeftLef,
							botRightLef, topRightLef);

					Vec3D topLeftRig = new Vec3D(C_LENGTH * col + C_LENGTH,
							C_HEIGHT, C_WIDTH * row + C_WIDTH);
					Vec3D botLeftRig = new Vec3D(C_LENGTH * col + C_LENGTH, 0,
							C_WIDTH * row + C_WIDTH);
					Vec3D botRightRig = new Vec3D(C_LENGTH * col + C_LENGTH, 0,
							C_WIDTH * row);
					Vec3D topRightRig = new Vec3D(C_LENGTH * col + C_LENGTH,
							C_HEIGHT, C_WIDTH * row);

					Rect squareRig = new Rect(topLeftRig, botLeftRig,
							botRightRig, topRightRig);

					CuboID cuboid = new CuboID(squareBot, squareTop, squareFro,
							squareBac, squareLef, squareRig);

					// bottom
					gl.glBegin(GL_QUADS);
					gl.glNormal3i(0, -1, 0);
					gl.glTexCoord2f(textureLeft[0], textureTop[0]);
					gl.glVertex3i(cuboid.getBottom().getTopLeft().getX(),
							cuboid.getBottom().getTopLeft().getY(), cuboid
									.getBottom().getTopLeft().getZ());
					gl.glTexCoord2f(textureLeft[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getBottom().getBotLeft().getX(),
							cuboid.getBottom().getBotLeft().getY(), cuboid
									.getBottom().getBotLeft().getZ());
					gl.glTexCoord2f(textureRight[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getBottom().getBotRight().getX(),
							cuboid.getBottom().getBotRight().getY(), cuboid
									.getBottom().getBotRight().getZ());
					gl.glTexCoord2f(textureRight[0], textureTop[0]);
					gl.glVertex3i(cuboid.getBottom().getTopRight().getX(),
							cuboid.getBottom().getTopRight().getY(), cuboid
									.getBottom().getTopRight().getZ());
					gl.glEnd();

					// top
					gl.glBegin(GL_QUADS);
					gl.glNormal3i(0, 1, 0);
					gl.glTexCoord2f(textureLeft[0], textureTop[0]);
					gl.glVertex3i(cuboid.getTop().getTopLeft().getX(), cuboid
							.getTop().getTopLeft().getY(), cuboid.getTop()
							.getTopLeft().getZ());
					gl.glTexCoord2f(textureLeft[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getTop().getBotLeft().getX(), cuboid
							.getTop().getBotLeft().getY(), cuboid.getTop()
							.getBotLeft().getZ());
					gl.glTexCoord2f(textureRight[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getTop().getBotRight().getX(), cuboid
							.getTop().getBotRight().getY(), cuboid.getTop()
							.getBotRight().getZ());
					gl.glTexCoord2f(textureRight[0], textureTop[0]);
					gl.glVertex3i(cuboid.getTop().getTopRight().getX(), cuboid
							.getTop().getTopRight().getY(), cuboid.getTop()
							.getTopRight().getZ());
					gl.glEnd();

					// front
					gl.glBegin(GL_QUADS);
					gl.glNormal3i(0, 0, 1);
					gl.glTexCoord2f(textureLeft[0], textureTop[0]);
					gl.glVertex3i(cuboid.getFront().getTopLeft().getX(), cuboid
							.getFront().getTopLeft().getY(), cuboid.getFront()
							.getTopLeft().getZ());
					gl.glTexCoord2f(textureLeft[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getFront().getBotLeft().getX(), cuboid
							.getFront().getBotLeft().getY(), cuboid.getFront()
							.getBotLeft().getZ());
					gl.glTexCoord2f(textureRight[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getFront().getBotRight().getX(),
							cuboid.getFront().getBotRight().getY(), cuboid
									.getFront().getBotRight().getZ());
					gl.glTexCoord2f(textureRight[0], textureTop[0]);
					gl.glVertex3i(cuboid.getFront().getTopRight().getX(),
							cuboid.getFront().getTopRight().getY(), cuboid
									.getFront().getTopRight().getZ());
					gl.glEnd();

					// back
					gl.glBegin(GL_QUADS);
					gl.glNormal3i(0, 0, -1);
					gl.glTexCoord2f(textureLeft[0], textureTop[0]);
					gl.glVertex3i(cuboid.getBack().getTopLeft().getX(), cuboid
							.getBack().getTopLeft().getY(), cuboid.getBack()
							.getTopLeft().getZ());
					gl.glTexCoord2f(textureLeft[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getBack().getBotLeft().getX(), cuboid
							.getBack().getBotLeft().getY(), cuboid.getBack()
							.getBotLeft().getZ());
					gl.glTexCoord2f(textureRight[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getBack().getBotRight().getX(), cuboid
							.getBack().getBotRight().getY(), cuboid.getBack()
							.getBotRight().getZ());
					gl.glTexCoord2f(textureRight[0], textureTop[0]);
					gl.glVertex3i(cuboid.getBack().getTopRight().getX(), cuboid
							.getBack().getTopRight().getY(), cuboid.getBack()
							.getTopRight().getZ());
					gl.glEnd();

					// left
					gl.glBegin(GL_QUADS);
					gl.glNormal3i(-1, 0, 0);
					gl.glTexCoord2f(textureLeft[0], textureTop[0]);
					gl.glVertex3i(cuboid.getLeft().getTopLeft().getX(), cuboid
							.getLeft().getTopLeft().getY(), cuboid.getLeft()
							.getTopLeft().getZ());
					gl.glTexCoord2f(textureLeft[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getLeft().getBotLeft().getX(), cuboid
							.getLeft().getBotLeft().getY(), cuboid.getLeft()
							.getBotLeft().getZ());
					gl.glTexCoord2f(textureRight[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getLeft().getBotRight().getX(), cuboid
							.getLeft().getBotRight().getY(), cuboid.getLeft()
							.getBotRight().getZ());
					gl.glTexCoord2f(textureRight[0], textureTop[0]);
					gl.glVertex3i(cuboid.getLeft().getTopRight().getX(), cuboid
							.getLeft().getTopRight().getY(), cuboid.getLeft()
							.getTopRight().getZ());
					gl.glEnd();

					// right
					gl.glBegin(GL_QUADS);
					gl.glNormal3i(1, 0, 0);
					gl.glTexCoord2f(textureLeft[0], textureTop[0]);
					gl.glVertex3i(cuboid.getRight().getTopLeft().getX(), cuboid
							.getRight().getTopLeft().getY(), cuboid.getRight()
							.getTopLeft().getZ());
					gl.glTexCoord2f(textureLeft[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getRight().getBotLeft().getX(), cuboid
							.getRight().getBotLeft().getY(), cuboid.getRight()
							.getBotLeft().getZ());
					gl.glTexCoord2f(textureRight[0], textureBottom[0]);
					gl.glVertex3i(cuboid.getRight().getBotRight().getX(),
							cuboid.getRight().getBotRight().getY(), cuboid
									.getRight().getBotRight().getZ());
					gl.glTexCoord2f(textureRight[0], textureTop[0]);
					gl.glVertex3i(cuboid.getRight().getTopRight().getX(),
							cuboid.getRight().getTopRight().getY(), cuboid
									.getRight().getTopRight().getZ());
					gl.glEnd();

					cuboids = new ArrayList<>();
					cuboids.add(cuboid);

				}
			}
		}

		texture[0].disable(gl);
	}

	// paredes
	private void drawwall() {
		gl.glBegin(GL_QUADS);
		gl.glNormal3f(0, 1, 0);
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				gl.glTexCoord2f(textureLeft[1], textureBottom[1]);
				gl.glVertex3f(j, 0, i);
				gl.glTexCoord2f(textureRight[1], textureBottom[1]);
				gl.glVertex3f(j, 0, i + 1f);
				gl.glTexCoord2f(textureRight[1], textureTop[1]);
				gl.glVertex3f(j + 1f, 0, i + 1f);
				gl.glTexCoord2f(textureLeft[1], textureTop[1]);
				gl.glVertex3f(j + 1f, 0, i);
			}
		}
		gl.glEnd();
	}

	private void drawXYZ(GL2 gl) {
		gl.glBegin(GL2.GL_LINES);
		// x
		// gl.glColor3f(1, 0, 0);
		gl.glVertex3f(0f, 0f, 0f);
		gl.glVertex3f(100f, 0f, 0f);
		// y
		// gl.glColor3f(0, 1, 0);
		gl.glVertex3f(0f, 0f, 0f);
		gl.glVertex3f(0f, 100f, 0f);
		// z
		// gl.glColor3f(0, 0, 1);
		gl.glVertex3f(0f, 0f, 0f);
		gl.glVertex3f(0f, 0f, 100f);

		gl.glEnd();

	}

	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W:
			frente = false;
			break;
		case KeyEvent.VK_S:
			volta = false;
			break;
		case KeyEvent.VK_A:
			esquerda = false;
			break;
		case KeyEvent.VK_D:
			direita = false;
			break;
		case KeyEvent.VK_E:
			salta = false;
			break;
		case KeyEvent.VK_Q:
			voltasalta = false;
			break;
		}
	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W:
			frente = true;
			break;
		case KeyEvent.VK_S:
			volta = true;
			break;
		case KeyEvent.VK_A:
			esquerda = true;
			break;
		case KeyEvent.VK_D:
			direita = true;
			break;
		case KeyEvent.VK_E:
			salta = true;
			break;
		case KeyEvent.VK_Q:
			voltasalta = true;
			break;
		}
	}

	public void mousePressed(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		dx = 0;
		dy = 0;
		ox = x;
		oy = y;
	}

	public void mouseMoved(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		dx += x - ox;
		ox = x;
		oy = y;
	}

	public void mouseDragged(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		dx += x - ox;
		dy += y - oy;
		ox = x;
		oy = y;
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
}