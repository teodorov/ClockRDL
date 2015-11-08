package ClockRDL.interpreter.frames;

/**
 * Created by ciprian on 07/11/15.
 */
public class PrimitiveRelationFrame extends PersistentFrame {
    int primitiveID;
    public PrimitiveRelationFrame(String name, int pID, AbstractFrame env) {
        super(name, env);
        this.primitiveID = pID;
    }

    public int getPrimitiveID() {
        return primitiveID;
    }
}
