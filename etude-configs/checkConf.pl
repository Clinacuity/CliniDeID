#!/usr/local/bin/perl
#parameter is an ETUDE config file
#checks for duplicate section names (those in [ ]) and duplicate values
#for the different tags

#First outputs all sections that were used 1 time (what should be there)
#and all values for each tag that were used one time

#Then prints sections that were duplicated and then duplicate values for a tag

open (IN, "$ARGV[0]") or die "Can't open $ARGV[0]: $!\n";
@in = <IN>;
close IN;

#shift @in until ($in[0] =~ m:Content Attribute:);

foreach $line (@in) {
	next if $line =~ m:(Begin|End) Attr:;
	if ($line =~ m#(\w+):\s+(\S+)#) {
		$parts{$1}{$2}++;
	} elsif ($line =~ m#\[ (\S+.*) \]#) {
		$sections{$1}++;
	}
}

foreach $section (sort keys %sections) {
	if ($sections{$section} > 1) {
		push @problems, "section $section duplicated $sections{$section} times \n";
	}
	else {
		print "$section used once\n";
	}
}
print "\n";
foreach $tag (sort keys %parts) {
  print "\n$tag: \n";
  foreach $value (sort keys %{$parts{$tag}}) { 	
	if ($parts{$tag}{$value} > 1) {
		push @problems, "tag $tag duplicated $value $parts{$tag}{$value}  times \n";
	}
	else {
		print "$tag used $value once\n";
	}
  }
}

print "\n@problems";