package org.mangocube.corenut.commons.util;

import java.util.*;

public abstract class DependencySortUtil<T> {
    /**
     * There are dependencies among units, so them may be sorted by dependencies.
     *
     * @param unitList test method unit list
     * @return sorted unit list
     */
    @SuppressWarnings("unchecked")
    public Collection<T> sortUnitsByReferenceCount(Collection<T> unitList) {
        Map<String, Integer> reference_count_map = new HashMap<String, Integer>(unitList.size());
        Map<String, T> unitMap = new HashMap<String, T>(unitList.size());
        for (T unit : unitList) {
            unitMap.put(getIdentifier(unit), unit);
            for (T other_unit : unitList) {
                //if there unit doesn't refer to other_unit, means no foreign key reference directly/indirectly.
                if (unit == other_unit || !isUnitRefExist(unit, other_unit)) continue;
                //unit refer to other_unit, count its points in reference_count_map.
                String other_unit_id = getIdentifier(other_unit);
                if (reference_count_map.containsKey(other_unit_id)) {
                    reference_count_map.put(other_unit_id, reference_count_map.get(other_unit_id) + 1);
                } else {
                    reference_count_map.put(other_unit_id, 1);
                }
            }
        }
        Map.Entry[] entries = getSortedHashtableByValue(reference_count_map);

        //after adding the referenced tables into the unitList, then add
        //those which have no referenced tables to the unitList
        List<T> sorted_tables = new ArrayList<T>();
        for (Map.Entry entry : entries) {
            sorted_tables.add(unitMap.remove(entry.getKey()));
        }
        sorted_tables.addAll(unitMap.values());
        return sorted_tables;
    }

    //sort the table map by reference count DESC
    @SuppressWarnings("unchecked")
    private Map.Entry[] getSortedHashtableByValue(Map h) {
        Set set = h.entrySet();
        Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[set.size()]);
        Arrays.sort(entries, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                Integer value1 = (Integer) ((Map.Entry) arg0).getValue();
                Integer value2 = (Integer) ((Map.Entry) arg1).getValue();
                return value2.compareTo(value1);
            }
        });
        return entries;
    }

    protected abstract String getIdentifier(T unit);

    /**
     * Detect unit dependency (whether the unit1 references unit2) using Breadth-First algorithm.
     * Note that this method simulate the recursive checking for dependencies.
     *
     * @param unit1 The first table.
     * @param unit2 The second table.
     * @return True if the first table reference the second table directly
     *         or indirectly, otherwise false.
     */
    protected boolean isUnitRefExist(T unit1, T unit2) {
        List<T> unitList = new LinkedList<T>();
        //first insert the unit1 into the queue
        unitList.add(unit1);
        while (unitList.size() > 0) {
            T popUnit = unitList.get(0);
            //iterate all references in this table
            for (T depUnit : getDependentUnits(popUnit)) {
                //if unit2 is the referenced table for unit1 then return true
                String refered_unit_id = getIdentifier(depUnit);
                if (getIdentifier(unit1).equalsIgnoreCase(refered_unit_id)) {
                    handleCycleDependency(unit1, popUnit);
                }
                if (getIdentifier(unit2).equalsIgnoreCase(refered_unit_id)) {
                    return true;
                } else {
                    //if not then check if the referenced table exists in the queue,
                    if (!isUnitExistAlready(unitList, refered_unit_id)) {
                        //if not then add it to the queue
                        unitList.add(depUnit);
                    }
                }
            }
            //remove the first element in the queue
            unitList.remove(0);
        }
        return false;
    }

    protected abstract Collection<T> getDependentUnits(T unit);

    protected abstract void handleCycleDependency(T unit, T depUnit);

    /**
     * Checks if the given table is already exists in the
     *
     * @param unitList tables to be sorted
     * @param id given table name
     * @return true if exist otherwise false
     */
    private boolean isUnitExistAlready(List<T> unitList, String id) {
        for (T unit : unitList) {
            if (getIdentifier(unit).equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }
}
