+!detect
  <-
  EnabledNormAnnot = norm(Id,enabled,Condition,Issuer,Content)[H|T];
  // get norms whose activation and maintenance condition are believed to be true and ignore norm instances
  .setof(EnabledNormAnnot, 
  	  EnabledNormAnnot
  	    & not .member(activation(_), [H|T]) 
  	    & Condition 
  	    & Content =.. [_,_,[_,MaintCond,_,_],_] 
  	    & MaintCond,
  	  CandidateNorms
  );
  for (.member(EnabledNormAnnot,CandidateNorms)) {
  	// get valid activation conditions
  	.setof(Condition, Condition, ValidConditions);
  	// get active norm instances with the same id and condition
    .setof(Condition, 
        norm(Id,_,Condition,_,_)[activation(_)|Annots] 
	      & not .member(deactivation(_),Annots) 
	      & not .member(fulfillment(_),Annots) 
	      & not .member(unfulfillment(_),Annots), 
        ActiveNormInstances
    );
    // for each norm whose activation condition hasn't triggered any currently active norm instance
    for ( .member(Condition, ValidConditions) & not .member(Condition, ActiveNormInstances)) {
      cartago.invoke_obj("java.lang.System",currentTimeMillis,Time);
      .add_annot(EnabledNormAnnot,activation(Time), Instance);
      +Instance;
      !!watch_norm_instance(Instance);
    }
  }.

+!watch_norm_instance(norm(Id,enabled,Condition,Issuer,Content)[activation(T)|Annots])
  : Content =.. [_,obligation,[_,MaintCond,Aim,Deadline],_]
  <-
  .wait(not MaintCond | Aim, Deadline);
  cartago.invoke_obj("java.lang.System",currentTimeMillis,Time);
  Instance = norm(Id,enabled,Condition,Issuer,Content)[activation(T)|Annots];
  
  if (not MaintCond) {
    .add_annot(Instance,deactivation(Time), FinishedInstance);
  } else {
    if (Aim) {
      .add_annot(Instance,fulfillment(Time), FinishedInstance);
    } else {
      .add_annot(Instance,unfulfillment(Time), FinishedInstance);
    }
    +FinishedInstance;
    !report(FinishedInstance);
  }.
  
+!report(FinishedInstance)
  <-
  .print("To do: +!report(FinishedInstance)").