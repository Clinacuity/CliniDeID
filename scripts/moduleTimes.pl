#!/usr/local/bin/perl

$DEID_DIR= $ENV{"DEID_DIR"};
$DEID_DIR.="/clinideid-legacy-app";
@in=`grep Cumulative $DEID_DIR/log/DeidLog.log`;
#open (IN, "t.log");
#@in=<IN>;
#close IN;
$count=0;

foreach $line (@in) {#for VoteAnnotator was 440074
	unless ($line =~ m/for\s+(\S+)\s+was\s+(\d+)\s+(\S+)/i) {
		print "issue with $line";
		next;
	}
	$thing = $1;
	$time = $2;
	$unit = $3;
	$totals{$thing}=$time;
	if ($thing eq 'VoteAnnotator') {
		$count++;
	}
#	$dif=$time-$lastTimes{$thing};
#	if (! exists $maxs{$thing} || $maxs{$thing} <$dif) { $maxs{$thing}=$dif;}
#	$lastTimes{$thing}=$time;
}

foreach $thing (sort keys %totals) {
	printf "Average for %25s %9d $unit\n",  $thing, $totals{$thing}/($count);
	$total+=$totals{$thing};
}
printf "\ttotal for just process methods of the above annotators: %5.2f $unit per document ($count documents)\n", $total/$count;