# Makefile for compiling and running the parallel Monte Carlo Minimization program

# Compiler and flags
JC = javac
JFLAGS = -d $(BINDIR) -sourcepath $(SRCDIR)

# Source and target directories
SRCDIR = src
BINDIR = bin

# Package structure
PACKAGE = ParallelMonteCarloMini

# List of Java source files
CLASSES = $(SRCDIR)/$(PACKAGE)/MonteCarloMinimizationParallel.java \
          $(SRCDIR)/$(PACKAGE)/SearchParallel.java \
          $(SRCDIR)/$(PACKAGE)/TerrainArea.java

# Compile the Java classes
default: $(CLASSES)
	@mkdir -p $(BINDIR)
	$(JC) $(JFLAGS) $^

# Define default arguments
DEFAULT_ARGS = 100 100 0.0 100.0 0.0 100.0 0.5

# Run the main class with default arguments
run-default:
	java -classpath $(BINDIR) $(PACKAGE).MonteCarloMinimizationParallel $(DEFAULT_ARGS)
	
# Run the main class with user-defined arguments
run:
	java -classpath $(BINDIR) $(PACKAGE).MonteCarloMinimizationParallel $(ARGS)

# Clean the compiled classes
clean:
	@rm -rf $(BINDIR)