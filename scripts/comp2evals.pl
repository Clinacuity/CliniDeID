#!/usr/local/bin/perl
$syntax = "comp2evals.pl file1 file2 where fileI are outputs from ETUDE | prettyPrintPipe.pl";
#  prints differences of TP FP FN scores
#  positive numbers indicate the second file had more than first, negative indicate less
$threshold='10';
#prints to STDERR differences in TP or FP > $threshold

open (IN1, $ARGV[0]) or die "$syntax\nCan't open $ARGV[0]: $!\n";
@in1=<IN1>;
open (IN2, $ARGV[1]) or die "$syntax\nCan't open $ARGV[1]: $!\n";
@in2=<IN2>;

#skip description and header rows
shift @in1;
shift @in2;
shift @in1;
shift @in2;

if ($#in1 != $#in2) {
	die "Number of lines differ $#in1  $#in2\n";
}

print "$ARGV[1]     -     $ARGV[0]\n";
$flag=0;
#if ($in[0] =~ m:AGGREGATE:) {
#	$flag=1;
#}
@bigDifference = ();
foreach $i (0 ..$#in1) {
	if ($in1[$i] !~ m:\d+\s+\d+:) {
		print "$in1[$i]";
		next;
	}
	$in1[$i]=~s:(\d+)\.(\w+)\s+(\w+):$1.$2-$3:;
	$in2[$i]=~s:(\d+)\.(\w+)\s+(\w+):$1.$2-$3:;
	$in1[$i]=~s:micro-average\s+AGGREGATE:micro-AGGREGATE:;
	$in2[$i]=~s:micro-average\s+AGGREGATE:micro-AGGREGATE:;
	@vals1=split (/\s+/, $in1[$i]);
	@vals2=split (/\s+/, $in2[$i]);

	if ($vals1[0] =~ m:\S:) {
		unshift @vals1, " ";
		unshift @vals2, " ";
	}

	chop($vals2[5]);
	chop($vals1[5]);
	chop($vals2[6]);
	chop($vals1[6]);
	chop($vals2[7]);
	chop($vals1[7]);

	if ($vals2[2]-$vals1[2] != -($vals2[4]-$vals1[4])) { #delta TP must equal delta FN
		push @errors, $vals1[1], " ";
		$flag = "off by " . ($vals2[2]-$vals1[2] -($vals2[4]-$vals1[4]));
	} else {
		$flag = "";
	}

	if ( $vals2[2]-$vals1[2] !=0 || $vals2[3]-$vals1[3] !=0 ) {
		printf "%19s: TP: %+5d, FP: %+5d, FN: %+5d, Precision %+6.1f%%, Recall %+6.1f%%, F1 %+6.1f%%\t\t$flag\n",
			$vals1[1], $vals2[2]-$vals1[2], $vals2[3]-$vals1[3], $vals2[4]-$vals1[4],
			$vals2[5]-$vals1[5], $vals2[6]-$vals1[6], $vals2[7]-$vals1[7];
		if (abs($vals2[2]-$vals1[2]) > $threshold ||   abs($vals2[3]-$vals1[3]) > $threshold) {
			push @bigDifference, sprintf "\t%19s: TP: %+5d FP: %+5d\n", $vals1[1], ($vals2[2]-$vals1[2]), ($vals2[3]-$vals1[3]);
		}
	}
}

if (@errors) {
	print STDERR "\n\n************FAIL*********** @errors\n";
}

if (@bigDifference) {
	print STDERR "TP and/or FP Difference > $threshold: \n @bigDifference\n";
}

if (@errors && @bigDifference) {
	exit 2;
} elsif (@errors) {
	exit 1;
} elsif (@bigDifference) {
	exit 3;
} else {
	exit 0;
}