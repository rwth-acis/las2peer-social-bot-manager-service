package i5.las2peer.services.socialBotManagerService.parser.drawing;

import i5.las2peer.services.socialBotManagerService.model.BotModelNode;

public class Vector {

    protected double x;
    protected double y;

    Vector(double x, double y) {
	this.set(x, y);
    }
    
    Vector(BotModelNode n1, BotModelNode n2) {

	double x = Math.abs(n1.getLeft() - n2.getLeft());
	double y = Math.abs(n1.getTop() - n2.getTop());
	this.set(x, y);

    }

    public void set(double x, double y) {
	this.x = x;
	this.y = y;
    }
    
    public void add(Vector v) {
	this.x = this.x + v.x;
	this.y = this.y + v.y;
    }

    public double norm() {

	double res = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	return res;

    }

    public void scalar(double s) {
	this.x = this.x * s;
	this.y = this.y * s;
    }

    public void unit() {

	double d = this.x + this.y;
	double x = this.x / d;
	double y = this.y / d;
	this.set(x, y);

    }

}
