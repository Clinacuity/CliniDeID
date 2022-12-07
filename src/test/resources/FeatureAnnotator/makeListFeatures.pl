#!/usr/local/bin/perl

#input file is created by 'grep FeatureVector file.raw.xml > file.features'
#assumed to have all the correct values. Output is file.java which is a method
#named makeFvfile that creates a List<FeatureVector> from the xmi
#that is then given to makeStringsFromFeatureVectors that combines values to string
#which is easier to compare

$file = shift @ARGV;
open (IN, "$file.features") or die "Can't open $file.features: $!\n";
@in=<IN>;
close IN;

chomp @in;
open (OUT, ">$file.java")or die "Can't create $file.java: $!\n";
	print  OUT "private List<FeatureVector> makeFv$file() {
		List<FeatureVector> correctFeatures=new ArrayList<>();
        FeatureVector fv;\n";

foreach $line (@in) {

	print  OUT "fv=new FeatureVector(jCas);\n";
	chop $line;
	chop $line;  #remove the trailing />
		$line=~ s:&lt;:<:g;
		$line=~ s:&gt;:>:g;
	@parts = split(/\s+/, $line);
	foreach $part (@parts) {
		next if ($part =~ m:FeatureVector|xmi|sofa:);	
		#string features need quotes around parameters, even if parameter is a number, other numbers are int and should have no quotes
		if ($part =~ m#(\w+fix\d+|.?word.*?|wv)="(.*?)"#) {#fv.setPrefix2(10);
			print OUT "fv.set", ucfirst $1, "(\"$2\");\n";
		} elsif ($part =~ s#="(\d+)"#($1)#) {
			print OUT "fv.set", ucfirst ($part), ";\n";
		} elsif ($part =~ s#="(.*?)"#("$1")#) {
			print OUT "fv.set", ucfirst ($part), ";\n";
		} else {
			#print "\t issue with $part\n";
		}
	}
	print OUT "correctFeatures.add(fv);\n\n";
}
print OUT "return correctFeatures;\n}\n";