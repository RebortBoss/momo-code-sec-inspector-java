/*
 * Copyright 2020 momosecurity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.immomo.momosec.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class SQLi {

    public static final Pattern whereInEndPattern          = Pattern.compile("(where|and|or)\\s+\\S+?\\s+in\\s*\\(?$", Pattern.CASE_INSENSITIVE);
    public static final Pattern whereInWithDollarPattern   = Pattern.compile("(where|and|or)\\s+\\S+?\\s+in\\s*\\(?\\s*\\$\\{", Pattern.CASE_INSENSITIVE);
    public static final Pattern likeEndPatterh             = Pattern.compile("\\S+?\\s+like\\s+('|\")%?$", Pattern.CASE_INSENSITIVE);

    public static final Pattern orderByOrGroupByEndPattern = Pattern.compile("(group|order)\\s+by\\s*([a-zA-Z0-9_\\-\\(\\)]+,?\\s{0,3}){0,5}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern havingEndPattern           = Pattern.compile("having\\s*([a-zA-Z0-9_\\-\\(\\)]+,?\\s{0,3}){0,5}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern limitEndPattern            = Pattern.compile("limit\\s*([a-zA-Z0-9_\\-\\(\\)]+,?\\s{0,3})?$", Pattern.CASE_INSENSITIVE);

    public static final Pattern placeholderPattern         = Pattern.compile("%(\\d\\$\\d{0,5})?s", Pattern.CASE_INSENSITIVE);
    public static final Pattern dollarVarPattern           = Pattern.compile("\\$\\{(\\S+?)\\}");


    /**
     * 判断数组是否有拼接SQL注入风险
     * 排除表名，部分字段名情况
     * @param fragments List<String>
     * @return boolean
     */
    public static boolean hasVulOnAdditiveFragments(@NotNull List<String> fragments) {
        return fragments.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .anyMatch(item ->
                    // group by ${field}
                    // order by id, ${field}
                    !(
                        orderByOrGroupByEndPattern.matcher(item).find() &&
                        !limitEndPattern.matcher(item).find()
                    ) &&
                    // having ${field} > 18
                    !havingEndPattern.matcher(item).find() &&
                    // from ${table}
                    !item.endsWith("from") &&
                    // (inner|outer) join ${table}
                    !item.endsWith("join") &&
                    // select ${field}
                    // select id, ${field}
                    !(
                        item.startsWith("select") &&
                        !item.contains("from")
                    ) &&
                    // update ${table}
                    !item.endsWith("update") &&
                    // update TABLE set ${field}
                    !(
                        item.startsWith("update") &&
                        item.endsWith("set")
                    ) &&
                    // insert into TABLE(${field})
                    // insert into TABLE(id, ${field})
                    !(
                        item.startsWith("insert into") &&
                        !item.contains("value")
                    ) &&
                    // insert into TABLE (${field})
                    !item.equals("(")
                );
    }
}
