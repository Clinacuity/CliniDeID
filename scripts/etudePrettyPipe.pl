#!/usr/local/bin/perl
#this is meant to take piped input from Etude with TP FP FN Precision Recall F1 columns 
@in=<STDIN>;
shift @in;  #progress bar ??

#open (IN, "t.txt");
#@in=<IN>;
#close IN;

$line = shift @in;			#field line
$line =~ s:Precision:Preci:;
@parts = split (/\s+/, $line);
$firstLine = sprintf ("%19s %5s %5s %5s %7s %7s %7s\n", @parts);

foreach $line (@in) {
	if ($line =~ m:\S:) {
		$line =~ s:^\s+::;
		$line =~ s:fully-contained:fully:;#so that the columns line 
		$line =~ s:All Patterns:all:i;
		$line =~ s:(micro|macro)-average\s+([a-z]+\s*[a-z]*\s*)?:$1 :; #ditch by type that is after macro
  		@parts = split (/\s+/, $line);
  		if ($line !~ m:fully|exact|partial:) {
	 		foreach $val (@parts[4..6]) {  #precision, recall, f1
				$val=sprintf("%6.1f%%", $val*100) unless ($val =~ m:[a-z]:i);  #change to percentages
			}
			$line = sprintf  ("%19s %5d %5d %5d %7s %7s %7s\n", @parts);# %6.1f%% %6.1f%% %6.1f%% \n", @parts);
		} else {#happens if doing more than 1 fuzzy-match-flag
			$line = sprintf "%19s\n", $parts[0];
		}
	}
	else {$line = "\n";}
}
#open (OUT, ">temp.txt");
#print OUT $firstLine, @in;
#close OUT;

print $firstLine, @in;
