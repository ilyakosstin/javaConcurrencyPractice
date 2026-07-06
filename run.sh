javac -cp lib/lombok.jar -processorpath lib/lombok.jar -d build/ solution/$1.java \
&& java -cp lib/lombok.jar -cp build/ solution.$1 