package ClockRDL.interpreter;

import ClockRDL.interpreter.values.NulValue;
import ClockRDL.model.declarations.*;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
import ClockRDL.model.kernel.Declaration;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;

import javax.lang.model.element.Name;
import java.util.Map;

/**
 * Created by ciprian on 20/10/15.
 */
public class InitialEnvironmentBuilder {

    private final Interpreter evaluator = new Interpreter();

    final ValueInitializer valueInitializer = new ValueInitializer();

    DeclarationsSwitch<Frame> declarationsSwitch = new DeclarationsSwitch<Frame>() {
        Frame currentFrame;
        @Override
        public Frame caseRelationInstanceDecl(RelationInstanceDecl object) {

            Frame frame = doSwitch(object.getRelation());

            for (Map.Entry<String, Expression> entry :  object.getArgumentMap()) {
                Value value = evaluator.eval(entry.getValue(), currentFrame);
                frame.bind(entry.getKey(), value);
            }
            return frame;
        }

        @Override
        public Frame casePrimitiveRelationDecl(PrimitiveRelationDecl object) {
            Frame myFrame = new Frame(object.getName(), currentFrame);
            currentFrame = myFrame;

            for (ArgumentDecl arg : object.getArguments()) {
                myFrame.bind(arg, NulValue.uniqueInstance);
            }

            for (NamedDeclaration decl : object.getDeclarations()) {
                Value initial = valueInitializer.initialValue(decl, myFrame);
                myFrame.bind(decl, initial);
            }

            currentFrame = myFrame.enclosingEnvironment;
            //currentFrame.bind(object, myFrame);
            return myFrame;
        }

        @Override
        public Frame caseCompositeRelationDecl(CompositeRelationDecl object) {
            Frame myFrame = new Frame(object.getName(), currentFrame);
            currentFrame = myFrame;

            for (ArgumentDecl arg : object.getArguments()) {
                myFrame.bind(arg, NulValue.uniqueInstance);
            }

            for (NamedDeclaration decl : object.getDeclarations()) {
                Value initial = valueInitializer.initialValue(decl, myFrame);
                myFrame.bind(decl, initial);
            }

            for (RelationInstanceDecl instance : object.getInstances()) {
                Frame instanceFrame = doSwitch(instance);
                myFrame.bind(instance, instanceFrame);
            }

            currentFrame = myFrame.enclosingEnvironment;
            //currentFrame.bind(object, myFrame);
            return myFrame;
        }
    };
}
