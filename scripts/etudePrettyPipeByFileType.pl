#!/usr/local/bin/perl
#this is meant to take piped input from Etude with TP FP FN Precision Recall F1 columns
#and --by-file-and-type
#will need to be updated with next Etude as its output got cleaned
@in=<STDIN>;
shift @in;  #progress bar

$line = shift @in;			#field line
$line =~ s:Precision:Preci:;
@parts = split (/\s+/, $line);
$firstLine = sprintf ("%14s       Type    %5s %5s %5s %7s %7s %7s\n", @parts);

foreach $line (@in) {
	$line =~ s:^\s+::;
	$line =~ s: by file::;
	@parts = split (/\s+/, $line);
	foreach $val (@parts[-3, -2, -1]) {  #precision, recall, f1
		$val*=100;  #change to percentages
	}
	if ($line =~ m: x (\w+):) {
		splice @parts, 1, 1;
		$line = sprintf ("%14s %12s %5d %5d %5d %6.1f%% %6.1f%% %6.1f%% \n", @parts);
	}
	else {
		$line = sprintf  ("%14s    AGGREGATE %5d %5d %5d %6.1f%% %6.1f%% %6.1f%% \n", @parts);
	}
}
print $firstLine, @in;
