package ClockRDL.compiler;

import ClockRDL.model.declarations.ArgumentDecl;
import ClockRDL.model.declarations.DeclarationsFactory;
import ClockRDL.model.declarations.PrimitiveFunctionDecl;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.HashMap;

/**
 * Created by ciprian on 22/10/15.
 */
public class GlobalScope extends Scope {

    public GlobalScope() {
        super("global");

        symbols = new HashMap<String, NamedDeclaration>() {{
            ArgumentDecl predicate = DeclarationsFactory.eINSTANCE.createArgumentDecl();
            predicate.setName("predicate");
            PrimitiveFunctionDecl fct = DeclarationsFactory.eINSTANCE.createPrimitiveFunctionDecl();
            fct.setName("assert");
            fct.getArguments().add(predicate);
            this.put("assert", fct);

            fct = DeclarationsFactory.eINSTANCE.createPrimitiveFunctionDecl();
            fct.setName("print");
            this.put("print", fct);
        }};
    }
}
