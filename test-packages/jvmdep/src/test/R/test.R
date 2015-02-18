
library(org.renjin.jvmdep)

testJvmMethod <- function() {
    stopifnot(executeJvmMethodInDependency() == 'ClassInTransitiveJvmDep')
}