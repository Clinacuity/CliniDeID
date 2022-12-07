#!/usr/local/bin/perl 
open (IN, "allRuns.txt") or die;
@in=<IN>;
close IN;

$count=0;
foreach $line (@in) {
	unless ($line =~ m/for\s+(\S+)\s+(\d+)\s+(\S+)/) {
		next;
	}
	$thing = $1;
	$time = $2;
	$unit = $3;
	$totals{$thing}+=$time;
	if ($time > $max{$thing}) {
		$max{$thing} = $time;
	}
	if ($min{$thing}==0 or $time < $min{$thing}) {
		$min{$thing} = $time;
	}
	if ($thing eq 'VoteAnnotator') {
		$count++;
	}
}

foreach $thing (sort keys %totals) {
	printf "%-25s average: %7.0f $unit\n", $thing, ($totals{$thing}-$max{$thing}-$min{$thing})/($count-2);
}