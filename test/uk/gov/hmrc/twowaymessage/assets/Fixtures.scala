/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.twowaymessage.assets

trait Fixtures {

  def conversationItem(id: String): String =
    s"""
       | {
       |     "renderUrl": {
       |         "url": "relUrl",
       |         "service": "service"
       |     },
       |     "statutory": false,
       |     "hash": "24d5d7da-1b11-4d38-a730-b2f952969440",
       |     "lastUpdated": 1554296147553,
       |     "status": "todo",
       |     "readTime": 1554296147548,
       |     "alerts": {
       |         "success": true,
       |         "alertTime": 1554296147552,
       |         "emailAddress": "2afd5ea4-b12e-4851-82d2-774cef6b3c83@test.com"
       |     },
       |     "alertDetails": {
       |         "data": {
       |         },
       |         "templateId": "templateId"
       |     },
       |     "alertFrom": "2013-12-01",
       |     "validFrom": "2013-12-01",
       |     "body": {
       |         "form": "2WSM-reply",
       |         "type": "2wsm-advisor",
       |         "paperSent": false,
       |         "issueDate": "2019-06-13",
       |         "replyTo": "5d021fbe5b0000200151779c",
       |         "threadId": "5d021fbe5b0000200151779d",
       |         "enquiryType": "p800",
       |         "adviser": {
       |             "pidId": "123"
       |         }
       |     },
       |     "subject": "Matt Test 1",
       |     "recipient": {
       |         "identifier": {
       |             "value": "8000045498",
       |             "name": "sautr"
       |         },
       |         "regime": "sa"
       |     },
       |     "content": "RGVhciBUZXN0VXNlciBUaGFuayB5b3UgZm9yIHlvdXIgbWVzc2FnZSBvZiAxMyBKdW5lIDIwMTkuPGJyPlRvIHJlY2FwIHlvdXIgcXVlc3Rpb24sIEkgdGhpbmsgeW91J3JlIGFza2luZyBmb3IgaGVscCB3aXRoPGJyPkkgYmVsaWV2ZSB0aGlzIGFuc3dlcnMgeW91ciBxdWVzdGlvbiBhbmQgaG9wZSB5b3UgYXJlIHNhdGlzZmllZCB3aXRoIHRoZSByZXNwb25zZS48YnI+SWYgeW91IHRoaW5rIHRoZXJlIGlzIHNvbWV0aGluZyBpbXBvcnRhbnQgbWlzc2luZywgdXNlIHRoZSBsaW5rIGF0IHRoZSBlbmQgb2YgdGhpcyBtZXNzYWdlIHRvIGZpbmQgb3V0IGhvdyB0byBjb250YWN0IEhNUkMuPGJyPlJlZ2FyZHM8YnI+TWF0dGhldyBHcm9vbTxicj5uSE1SQyBkaWdpdGFsIHRlYW0u",
       |     "id": "$id"
       | }
     """.stripMargin

  def conversationItems(id1: String, id2: String): String =
    s"""
       | [
       | ${conversationItem(id1)},
       | ${conversationItem(id2)}
       | ]
         """.stripMargin
}
