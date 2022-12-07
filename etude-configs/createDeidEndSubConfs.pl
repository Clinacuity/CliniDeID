#!/usr/local/bin/perl -w

open (IN, "UnifiedDeid-EndToEnd-Split.conf") or die "Can't open: $!\n";
@in=<IN>;
close IN;

@options = qw (MIRA SVM oldCRF RNN);
# MIRA,SVM oldCRF,MIRA oldCRF,MIRA,SVM oldCRF,SVM);
foreach $opt1 (@options) {
	foreach $opt2 (@options) {
		if ($opt1 lt $opt2) {#prevents duplicates and results in consistent ordering
			push @addons, "$opt1,$opt2";
		}
	}
}

push @options, "PostPHI", "Regex", @addons;
print join ("\t", sort @options), "\n";

foreach $option (@options) {
	$filename=$option;
	$filename =~ s:,:-:g;
	open (OUT, ">UnifiedDeid-EndToEnd-Split-$filename.conf") or die "Can't create $filename $!\n";

	@copy = @in;
	foreach $line (@copy) {
		next unless $line =~ m:^XPath:;
		$line =~ s:"\]\s*$:"][\@method="$option"]\n:
	}

	print OUT @copy;
	close OUT;
#	last;
}

