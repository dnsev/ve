S = \\
SOURCE_DIRECTORY = src
OBJECT_DIRECTORY = bin
CC = javac -Xlint -d ..$(S)$(OBJECT_DIRECTORY) -sourcepath .


.PHONY: build
build: clearscreen ..$(S)app$(S)Videncode.jar
	@echo "Project built"

.PHONY: g
g: clearscreen
	@java -jar ..$(S)app$(S)Videncode.jar

.PHONY: run
run: clearscreen build
	@java -jar ..$(S)app$(S)Videncode.jar

.PHONY: clean
clean: clearscreen
#	@del $(OBJECT_DIRECTORY)$(S)com /s /f /q > NUL 2> NUL
	@rmdir $(OBJECT_DIRECTORY)$(S)com /s /q > NUL 2> NUL
	@del ..$(S)app$(S)Videncode.jar > NUL 2> NUL

.PHONY: clearscreen
clearscreen:
	@cls



..$(S)app$(S)Videncode.jar: classes $(wildcard $(OBJECT_DIRECTORY)/*.json) $(wildcard $(OBJECT_DIRECTORY)/res/*)
	@del $@ > NUL 2> NUL
	@cd $(OBJECT_DIRECTORY) && jar cvfme ..$(S)$@ .manifest.txt com/github/dnsev/videncode/Main res$(S)* *.json version com$(S)* && cd ..

classes: $(wildcard $(SOURCE_DIRECTORY)/*.java)
	@cd $(SOURCE_DIRECTORY) && $(CC) $(patsubst $(SOURCE_DIRECTORY)/%,%,$^) && cd ..
