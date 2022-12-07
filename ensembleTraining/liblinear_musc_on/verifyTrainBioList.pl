open (IN, "bioDeid_musc.txt");
@in = <IN>;
$old =0;
for ($i=0; $i< @in; $i+=2) {
	if (  (($type1, $n1)=$in[$i]=~  m:B-(\w+)\t(\d+): )
    &&   (($type2, $n2)=$in[$i+1]=~m:I-(\w+)\t(\d+):) ) {
		unless ($type1 eq $type2 && $n2 == $n1 +1 && $n1 == $old +1) {
			print "issue with lines $i:\n\t$in[$i]\t$in[$i+1]";
		}
		$subsFound{$type1}=1;
		$old = $n2;	
	}
}

open (IN, "../musc_type.txt");
chomp (@types=<IN>);
close IN;

foreach $line (@types) {
	($sub, $parent) = split /,/, $line;
	if (exists $parentMap{$sub}) {
		print "$sub duplicated, $parentMap{$sub} and $parent\n";
	} elsif (!exists $subsFound{$sub}) {
		print "$sub not found in first list\n";
	} else {
		delete $subsFound{$sub};
		$parentMap{$sub}=$parent;
	}
}
print "remaining: ", keys %subsFound, " not found in second list\n";