+!sublist(_,0,N,[]): N<1. // N is a floating point number

+!sublist([_A|B],M,N,S)
  : M>0 & M<N
  <- !sublist(B,M-1,N-1,S).
  
+!sublist([A|B],M,N,S)
  : M==0 & M<N & N2=N-1
  <- 
  !sublist(B,0,N2,D);
  S=[A|D].

+!evaluate(norm(NormId,_,_,_,_))
  <-
  // Lets first try to get the linked sanctions
  ?link(NormId,Sanctions);
  
  //Shuffle list with linked sanctions
  .shuffle(Sanctions,ShuffledSanctions);
  
  // Get a random number of linked sanctions
  NumSanctions = math.random(.length(Sanctions) + 1);
  !sublist(ShuffledSanctions, 0, NumSanctions, SSne);
  
  .puts("I evaluated the case and the sanctions should be #{SSne}");
  !!order_execution(SSne).
  
+!order_execution(SSne)
  <-
  RoleSpec = role(Role,_,SuperRoles,_,_,_,_);
  for (.member(S,SSne)) {
	.setof(Role,specification(group_specification(_,RolesSpecs,[],properties(_))) & .member(RoleSpec,RolesSpecs) & .member(executor,SuperRoles),ExecutorRoles);
	.setof(Agent,play(Agent,R,_) & .member(R,ExecutorRoles),ExecutorAgents);
	
	.shuffle(ExecutorAgents,[Agent|_]);
	
	.puts("Ordering execution of #{S} to #{Agent}");
	.send(Agent, achieve, execute(S));
  }
  .

  