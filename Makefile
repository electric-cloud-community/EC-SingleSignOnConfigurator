#
# Makefile responsible for building the EC-SingleSignOnConfigurator plugin
#
# Copyright (c) 2005-2012 Electric Cloud, Inc.
# All rights reserved

SRCTOP=..
include $(SRCTOP)/build/vars.mak

NTESTINCLUDES += -Isrc/test/ntest -Isrc/main/resources/project/lib

build: package
ntest: NTESTFILES ?= src/test/ntest
unittest: ntest junit

#systemtest: start-selenium test-setup test-run stop-selenium
#systemtest: test-setup test-run

#test-setup:
#	$(EC_PERL) systemtest/setup.pl $(TEST_SERVER) $(PLUGINS_ARTIFACTS)

# test-run: NTESTFILES ?= systemtest

# test-run:
#	@-mkdir -p "$(OUTDIR)/$(ARCH)"
#	COMMANDER_PLUGIN_PERL="../../../staging/agent/perl" \
#	COMMANDER_JOBSTEPID="" \
#	COMMANDER_DEBUG=1 \
#	COMMANDER_DEBUGFILE="$(OUTDIR)/$(ARCH)/systemtest.log" \
#	$(EC_PERL) $(NTEST) --testout $(OUTDIR)/$(ARCH)/systemtest \
#		--target $(TEST_SERVER) $(NTESTARGS) $(NTESTFILES)

include $(SRCTOP)/build/rules.mak
