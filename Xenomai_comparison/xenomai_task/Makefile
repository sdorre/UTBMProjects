###### CONFIGURATION ######
DEST_DIR=./bin
ARMADEUS_BASE_DIR=/mnt/gi/TR52/TP/armadeus-3.2
include $(ARMADEUS_BASE_DIR)/Makefile.in
 
XENO=$(ARMADEUS_ROOTFS_DIR)/usr/xenomai
 
CC:=$(ARMADEUS_TOOLCHAIN_PATH)/arm-linux-gcc
LD:=$(ARMADEUS_TOOLCHAIN_PATH)/arm-linux-ld
CXX:=$(ARMADEUS_TOOLCHAIN_PATH)/arm-linux-g++
AS:=$(ARMADEUS_TOOLCHAIN_PATH)/arm-linux-as
NM:=$(ARMADEUS_TOOLCHAIN_PATH)/arm-linux-nm
AR:=$(ARMADEUS_TOOLCHAIN_PATH)/arm-linux-ar
SIZE:=$(ARMADEUS_TOOLCHAIN_PATH)/arm-linux-size
OBJCOPY:=$(ARMADEUS_TOOLCHAIN_PATH)/arm-linux-objcopy
 
EXEC=exec_RT
 
SRC= $(wildcard *.c)
OBJ= $(SRC:.c=.o)
CFLAGS=-g -W -Wall -I$(XENO)/include -I$(XENO)/include/native  -I$(XENO)/include/rtdm -D_GNU_SOURCE -D_REENTRANT
LDFLAGS=-L$(XENO)/lib -Xlinker -rpath $(XENO)/lib -Xlinker $(XENO)/lib/libnative.a $(XENO)/lib/librtdm.a -lpthread -lnative -lrtdm -nostartfiles
 
$(EXEC): $(OBJ)
	$(CC) -o $@ $^ $(LDFLAGS)
 
$(OBJ): $(SRC)
	$(CC) $(CFLAGS) -o $@ -c $<
 
all: $(EXEC)
 
clean:
	rm -rf $(OBJ)
	rm -rf $(EXEC)
	rm -f *.c~ *.h~ Makefile~
 
install: $(EXEC)
	@mkdir -p $(DEST_DIR)/$(EXEC)
	echo "$(EXEC):native:!./$(EXEC);popall:control_c" > $(DEST_DIR)/$(EXEC)/.runinfo
	cp -f $(EXEC) $(DEST_DIR)/$(EXEC)
 
mrproper: clean
	rm -rf $(DEST_DIR)/$(EXEC)
 
.PHONY: all install clean mrproper
