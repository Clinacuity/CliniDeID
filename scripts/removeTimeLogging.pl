#!/usr/local/bin/perl -w

$DEID_DIR=$ENV{"DEID_DIR"};
$DEID_DIR.="/clinideid-legacy-app";
chomp (@files=`ls $DEID_DIR/src/main/java/com/clinacuity/deid/ae/*.java`);
push @files, "$DEID_DIR/src/main/java/com/clinacuity/deid/ae/regex/impl/RegExAnnotatorThread.java";
#regex needs */ before process
foreach $file (@files) {
	($name) = $file=~m:$DEID_DIR/src/main/java/com/clinacuity/deid/ae/(\S+?)\.java:o;
	open (IN, "$file") or die "Can't open $file: $!\n";
	@in=<IN>;
	close IN;

	$index=0;
	while ($index < @in && $in[$index] !~ m:public void process\(|public void run\s*\(:) {$index++;}
	if (@in == $index) {
		print "No process or run found in $name\n";
		next;
	}
	#-1 line will be @Override, -2 will be timer creation
	if ($name ne "RegExAnnotator") {
		$in[$index-2]='';#timer
	} else {
		$in[$index-2]='';
	}
	$in[$index+1]='';#start
	$in[$index+2]='';#processTimed
	$in[$index+3]='';#stop
	$in[$index+4]='';#printCumulative
	$in[$index+5]='';#}
	$in[$index+6]='';#void processTimesed

	open (OUT, ">$file") or die "Can't write: $!\n";
	print OUT @in;
	close OUT;
}
