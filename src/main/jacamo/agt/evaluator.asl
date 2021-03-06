+!sublist(_,0,N,[]): N<1. // N is a floating point number

+!sublist([_A|B],M,N,S)
  : M>0 & M<N
  <- !sublist(B,M-1,N-1,S).
  
+!sublist([A|B],M,N,S)
  : M==0 & M<N & N2=N-1
  <- 
  !sublist(B,0,N2,D);
  S=[A|D].

+!evaluate(norm(NormId,_,_,_,_,linked_sanctions(Sanctions)))
  <-
  // Get only enabled sanctions
  .findall(Id, sanction(Id,enabled,_,_,_), EnabledSanctions);

  // Shuffle list with linked sanctions
  .shuffle(EnabledSanctions,ShuffledSanctions);

  // Get a random number of linked sanctions
  NumSanctions = math.random(.length(EnabledSanctions) + 1);
  !sublist(ShuffledSanctions, 0, NumSanctions, SSne);

  .puts("I evaluated the case and the sanctions should be #{SSne}");
  !!order_execution(SSne).

+!order_execution(SSne).
//  <-
//  Choose executor
//  .send(Executor, achieve, execute(S));
//  .
