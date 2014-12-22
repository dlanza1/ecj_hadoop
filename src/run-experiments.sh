problem_name="parity"
java ec.Evolve -file ec/app/parity/parity-groups.params -p jobs=30 -p breed.groups=1 -p generations=50 -p stat.file=${problem_name}.1.stat
for groups in 512 256 128 64 32 16 8 4 2
do
    java ec.Evolve -file ec/app/parity/parity-groups.params \
                    -p jobs=30 \
                    -p breed.groups=${groups} \
                    -p generations=75 \
                    -p stat.file=${problem_name}.${groups}.stat
done

problem_name="ant"
java ec.Evolve -file ec/app/ant/ant-groups.params -p jobs=30 -p breed.groups=1 -p generations=50 -p stat.file=${problem_name}.1.stat
for groups in 512 256 128 64 32 16 8 4 2
do
    java ec.Evolve -file ec/app/ant/ant-groups.params \
                    -p jobs=30 \
                    -p breed.groups=${groups} \
                    -p generations=75 \
                    -p stat.file=${problem_name}.${groups}.stat
done

problem_name="lawnmover"
java ec.Evolve -file ec/app/lawnmower/noadf-groups.params -p jobs=30 -p breed.groups=1 -p generations=50 -p stat.file=${problem_name}.1.stat
for groups in 512 256 128 64 32 16 8 4 2
do
    java ec.Evolve -file ec/app/lawnmower/noadf-groups.params \
                    -p jobs=30 \
                    -p breed.groups=${groups} \
                    -p generations=75 \
                    -p stat.file=${problem_name}.${groups}.stat
done

problem_name="multiplexer"
java ec.Evolve -file ec/app/multiplexer/11-groups.params -p jobs=30 -p breed.groups=1 -p generations=50 -p stat.file=${problem_name}.1.stat
for groups in 512 256 128 64 32 16 8 4 2
do
    java ec.Evolve -file ec/app/multiplexer/11-groups.params \
                    -p jobs=30 \
                    -p breed.groups=${groups} \
                    -p generations=75 \
                    -p stat.file=${problem_name}.${groups}.stat
done
 
problem_name="regression"
java ec.Evolve -file ec/app/regression/sexticerc-groups.params -p jobs=30 -p breed.groups=1 -p generations=50 -p stat.file=${problem_name}.1.stat
for groups in 512 256 128 64 32 16 8 4 2
do
    java ec.Evolve -file ec/app/regression/sexticerc-groups.params \
                    -p jobs=30 \
                    -p breed.groups=${groups} \
                    -p generations=75 \
                    -p stat.file=${problem_name}.${groups}.stat
done 

problem_name="royaltree"
java ec.Evolve -file ec/app/royaltree/royaltree-groups.params -p jobs=30 -p breed.groups=1 -p generations=500 -p stat.file=${problem_name}.1.stat
for groups in 2048 1024 512 256 128 64 32 16 8 4 2
do
    java ec.Evolve -file ec/app/royaltree/royaltree-groups.params \
                    -p jobs=30 \
                    -p breed.groups=${groups} \
                    -p generations=750 \
                    -p stat.file=${problem_name}.${groups}.stat
done