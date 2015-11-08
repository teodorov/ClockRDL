

####Initialized Clocks and Internal Clocks
If one clock in a relation is not mapped at instantiation there are two possible interpretations:
- if the clock does not have a default value, an error is thrown
- if the clock has a default value a new clock is created. If the relation is intantiated N times, there will be N such clocks.

A clock internal to a relation will contribute **one clock** for each instantiation of a relation. These clocks are different, independent, and constrained only by the relations to which they are passed as argument.

While this feature is nice it posses some challenges for:
- naming the clocks, they should have a fully qualified name with respect to the instantiation hierarchy
- for finding the clocks, we should be able to follow a hierarchical name down the instantiation hierarchy

####TODO
- [x] internal clocks does not need the clock, we can declare them with clock[name] instead of x := clock[name]
- [ ] check that multiple instances with default and internal clocks do not share these clocks in ClockRDL2Smalltalk.
- [ ] implement support for assert, print, array, and other global functions in ClockSystem[Smalltalk]
- [20%] add shared variable support, what about using 'ref' instead of the argument list?
- [ ] add possibility to specify clock negation in clock vector
- [ ] what happens when no clocks? maybe I we can offer a choice to the user. In this case ClockSystem does 3(a)
    - (a) everything without clocks executes in the same step (x,y)
    - (b) asynchronous interleaving (x,-) (-,y)
    - (c) all possibilities (-,-) (x,-) (-,y) (x,y)
- [ ] add primitive always
- [ ] Currently the library import is very shitty, I should reify the importDecl in the model, stop doing name resolution
 during AST creation but in an subsequent pass over the instantiated model, and use the symbol table to resolve stuff.
- [ ] define a clear error handling strategy for Parsing 

####Questions
1. Do we need the possibility to convert arrays to queues?
2. Do we need to care about the performance of the interpreter?
3. If we care about the performance, does Truffle gives good results?
4. Do we care about recursive functions?



#####Primitive clocks in CCSL

######Primitive clock never
This is a clock that is never allowed to tick.
If clock negation is allowed in the clock vector specification this clock can simply be defined as the following relation:

    relation never
        clock clk;
    {
        { !clk };
    }

######Primitive clock always
this is a clock that always ticks no matter what.
The only way to enable this behaviour would be to have it as a primitive clock. **clock[always]**

######Primitive clock forceOne
The forceOne clock is a clock that ticks once and then dies.
The forceOne clock makes sense only if we consider concatenation.

This clock can be defined as the following relation:

	relation forceOne
		clock one;
		var alive := true;
	{
		[alive] { one } [ alive := ! alive ]; //the clock dies
	}

######Primitive clock forceZero
The forceZero clock is a clock that does not tick and then dies.
The forceZero clock makes sense only if we consider concatenation.

If clock negation is allowed in the clock vector specification this clock can simply be defined as the following relation:

    relation forceZero
		clock zero;
		var alive := true;
	{
		[alive] { !zero } [ alive := ! alive ]; //the clock dies
	}

######Primitive relation concatenation
The concatenation relation makes sense only if we consider clock death.
The concatenation relation is a special composite relation which is used to build a sequential composition of constraints.
In a concatenation relation only the **currently active** relation constrains the system.
When the **currently active** relation **dies** the next one becomes **active**, and so on.

######Clock death and relation death
According to the CCSL specification some expressions die. This is used to define concatenation.
Considering our automaton-based interpretation of CCSL one way to implement clock and relation death is:

- define one clock in a relation as a clock that can *die*
- define the conditions under which it dies, currently *CCSL* say something like "the clock ticks" and then the expression dies
- when a clock is death it should not be used anymore when computing the *ticking* clocks
- "the expression dies" can be interpreted as: when the dying clock is dead its containing relation does not constrain the system anymore
- if the dead expression is the **currently active** one in a concatenation then the concatenation enables the next relation
- if the dead expression is not part of a concatenation then it is ignored during the following steps in the composition

The only requirement imposed by the *clock death* is that during the computation of the *ticking* clocks we should be able to say if the clock is dead or not.
However the relation death might pose a problem in construction and interpretation of the system configuration.

If we suppose that the configuration is a list of tuples (one tuple for each primitive relation in the system)
>
->(v<sub>1</sub>, ... , v<sub>n</sub>)
-> (v<sub>1</sub>, ... , v<sub>n</sub>)
-> (v<sub>1</sub>, ... , v<sub>n</sub>)

Then if a relation dies we can simply put an empty tuple at its corresponding position
>
-> (v<sub>1</sub>, ... , v<sub>n</sub>)  *alive*
-> ()                                    *dead*
-> (v<sub>1</sub>, ... , v<sub>n</sub>)  *alive*

The configuration for the concatenation relation would be a pair with the car being the id of the **currently active** relation, and the cdr the tuple corresponding to that relation.
>
-> (activeID (v<sub>1</sub>, ... , v<sub>n</sub>))

If ever there are a lot of relations that die, and if we define relation **birth** as the dynamic introduction of 
constraints in the system we might need to implement a **garbage colletion* strategy for dead relations. In this situation
the empty tuples would be replaced by the newly born relations. However in this case each tuple would be contained in a 
versioning tuple, which is similar to the one used for concatenation.

>
->(version (v<sub>1</sub>, ... , v<sub>n</sub>))

######Relation birth
Relation birth can be used to coordinate systems where *execution units* can appear and disappear during execution.
For example in an OS environment semaphores are created, task may be spawn, etc.