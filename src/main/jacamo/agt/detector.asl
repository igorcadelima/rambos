@detectorCapability
+!detect(NSe)
  <-
  NormTemplate = norm(Id,enabled,Cond,I,Cont);
  .findall(NormTemplate, NormTemplate & .eval(X, Cond) & X, NSe).