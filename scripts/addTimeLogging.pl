#!/usr/local/bin/perl -w

$DEID_DIR=$ENV{"DEID_DIR"};
$DEID_DIR.="/clinideid-legacy-app";
chomp (@files=`ls $DEID_DIR/src/main/java/com/clinacuity/deid/ae/*.java`);
$regex="$DEID_DIR/src/main/java/com/clinacuity/deid/ae/regex/impl/RegExAnnotatorThread.java";

$newCodeProcess = <<'END_CODE';
    public static com.clinacuity.deid.util.SimpleTimer timer = new com.clinacuity.deid.util.SimpleTimer();
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        timer.start();
        processTimed(jCas);
        timer.stopCumulative();
END_CODE

$newCodeRun = <<'END_CODE';
    public static com.clinacuity.deid.util.SimpleTimer timer = new com.clinacuity.deid.util.SimpleTimer();

    public void run() {
        timer.start();
        processTimed();
        timer.stopCumulative();
END_CODE

foreach $file (@files) {
	($name) = $file=~m:$DEID_DIR/src/main/java/com/clinacuity/deid/ae/(\S+?)\.java:o;
	#print "$file, $name\n";
	open (IN, "$file") or die "Can't open $file: $!\n";
	@in=<IN>;
	close IN;

	$index=0;
	while ($index < @in && $in[$index] !~ m:public void process\s*\(|public void run\s*\(:) {$index++;}
	if (@in == $index) {
		print "No process or run found in $name\n";
		next;
	}
	#-1 line will be @Override
	if ($in[$index] =~ m:public void proces:) {
		$in[$index-1]=$newCodeProcess . "        timer.printCumulative(\"$name\t\", 1000000);\n    }\n    public void processTimed(JCas jCas) throws AnalysisEngineProcessException {\n";
		$in[$index]='';
	} else {
		$in[$index-1]=$newCodeRun . "        timer.printCumulative(\"$name\t\", 1000000);\n    }\n    public void processTimed() {\n";
		$in[$index]='';
	}
	open (OUT, ">$file") or die "Can't write: $!\n";
	print OUT @in;
	close OUT;
}

#regex must be a little different
open (IN, $regex) or die "Can't open Regex: $!\n";
@in=<IN>;
close IN;
$index=0;
while ($index < @in && $in[$index] !~ m:public void run\s*\(:) {$index++;}
if (@in == $index) {
	print "No run found in Regex\n";
	next;
}
#-1 line will be @Override
$in[$index]=$newCodeRun . "        timer.printCumulative(\"RegExAnnotator\t\", 1000000);\n    }\n    public void processTimed() {\n";
open (OUT, ">$regex") or die "Can't write $regex: $!\n";
print OUT @in;
close OUT;



