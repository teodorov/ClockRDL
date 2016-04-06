package ClockRDL.interpreter;

import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.frames.GlobalFrame;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.Stack;

/**
 * Created by ciprian on 25/10/15.
 */
public class Environment {
    GlobalFrame globalFrame;
    Memory memory;
    Stack<AbstractFrame> executionStack = new Stack<>();
    public Value returnRegister = null;

    public Environment() {
        this(new GlobalFrame());
    }

    public Environment(GlobalFrame frame) {
        this.globalFrame = frame;
        executionStack.push(frame);
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public Memory getMemory() {
        return memory;
    }

    public NamedDeclaration find(String name) {
        return currentFrame().find(name);
    }

    public AbstractFrame currentFrame() {
        return executionStack.peek();
    }

    public void push(AbstractFrame frame) {
        executionStack.push(frame);
    }

    public AbstractFrame pop() {
        return executionStack.pop();
    }

    public Value lookup(NamedDeclaration decl) {
        return currentFrame().lookup(decl, memory);
    }

    public Value lookup(String decl) {
        return currentFrame().lookup(decl, memory);
    }

    public void bind(NamedDeclaration decl, Value value) {
        currentFrame().bind(decl, value, memory);
    }

    public void update(NamedDeclaration decl, Value value) {
        currentFrame().update(decl, value, memory);
    }
}
